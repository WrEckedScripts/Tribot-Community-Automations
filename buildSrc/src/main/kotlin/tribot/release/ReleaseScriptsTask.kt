package tribot.release

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.Serializable
import java.time.Duration

/** One releasable subproject: its manifest plus the zip its zipSources task built. */
data class ReleaseModule(
    val name: String,
    val manifestFile: File,
    val zipFile: File,
) : Serializable

/**
 * Publishes every manifest-bearing subproject to the TriBot website. Modules are
 * released independently so one bad manifest or backend hiccup cannot block the rest
 * of the nightly run; failures are collected and reported at the end.
 */
abstract class ReleaseScriptsTask : DefaultTask() {

    @get:Internal
    abstract val modules: ListProperty<ReleaseModule>

    @get:Internal
    abstract val dryRun: Property<Boolean>

    @get:Internal
    abstract val onlyModule: Property<String>

    init {
        // Remote state can change between runs, so the task must always execute.
        outputs.upToDateWhen { false }
    }

    private enum class Status(val label: String) {
        RELEASED("released"),
        SKIPPED("skipped"),
        PLANNED("planned"),
        FAILED("failed"),
    }

    private data class ModuleResult(
        val module: String,
        val script: String?,
        val status: Status,
        val detail: String,
    )

    private data class Env(val baseUrl: String, val apiKey: String, val authorUserId: Long)

    @TaskAction
    fun release() {
        val selected = modules.get()
        if (selected.isEmpty()) {
            if (onlyModule.isPresent) {
                throw GradleException(
                    "No releasable subproject named \"${onlyModule.get()}\". A subproject becomes " +
                        "releasable by adding a tribot-script.json manifest to its root directory."
                )
            }
            logger.lifecycle("No subprojects contain a tribot-script.json manifest; nothing to release.")
            return
        }

        val dry = dryRun.getOrElse(false)
        val env = if (dry) null else loadEnv()
        if (dry) {
            logger.lifecycle("Dry run: validating manifests and zips only, no API calls will be made.")
        }

        val results = selected.map { releaseModule(it, env) }
        printSummary(results, dry)
        appendGithubStepSummary(results, dry)

        val failed = results.filter { it.status == Status.FAILED }
        if (failed.isNotEmpty()) {
            throw GradleException(
                "releaseScripts failed for ${failed.size} module(s): ${failed.joinToString(", ") { it.module }}"
            )
        }
    }

    private fun loadEnv(): Env {
        val missing = listOf("TRIBOT_API_BASE_URL", "TRIBOT_API_KEY", "TRIBOT_AUTHOR_USER_ID")
            .filter { System.getenv(it).isNullOrBlank() }
        if (missing.isNotEmpty()) {
            throw GradleException(
                "Missing required environment variable(s): ${missing.joinToString(", ")}. " +
                    "Set them, or pass -PreleaseDryRun=true to validate without releasing."
            )
        }
        val authorUserId = System.getenv("TRIBOT_AUTHOR_USER_ID").trim().toLongOrNull()
            ?: throw GradleException("TRIBOT_AUTHOR_USER_ID must be a numeric user id.")
        return Env(
            baseUrl = System.getenv("TRIBOT_API_BASE_URL").trim().trimEnd('/'),
            apiKey = System.getenv("TRIBOT_API_KEY").trim(),
            authorUserId = authorUserId,
        )
    }

    private fun releaseModule(module: ReleaseModule, env: Env?): ModuleResult {
        var scriptName: String? = null
        return try {
            val manifest = ScriptManifest.load(module.name, module.manifestFile)
            scriptName = manifest.name
            if (!module.zipFile.isFile) {
                throw GradleException(
                    "expected ${module.zipFile} to exist after zipSources; was the task output relocated?"
                )
            }
            if (env == null) {
                ModuleResult(module.name, manifest.name, Status.PLANNED, plan(module, manifest))
            } else {
                releaseToBackend(module, manifest, env)
            }
        } catch (e: Exception) {
            logger.error("[${module.name}] release failed: ${e.message}")
            ModuleResult(module.name, scriptName, Status.FAILED, e.message ?: e.javaClass.simpleName)
        }
    }

    private fun plan(module: ReleaseModule, manifest: ScriptManifest): String {
        val target = manifest.scriptId?.let { "scriptId $it" } ?: "exact name match for \"${manifest.name}\""
        val sizeKb = module.zipFile.length() / 1024
        return "would resolve via $target, create the script if missing, sync metadata " +
            "(categories: ${manifest.categories.joinToString(", ")}; isCommunity: ${manifest.isCommunity}), " +
            "and upload ${module.zipFile.name} ($sizeKb KB) if the remote version differs from ${manifest.version}"
    }

    private fun releaseToBackend(module: ReleaseModule, manifest: ScriptManifest, env: Env): ModuleResult {
        val client = TribotApiClient(env.baseUrl, env.apiKey)
        val remote = resolveRemote(client, manifest, env.authorUserId)

        if (remote == null) {
            if (manifest.scriptId != null) {
                throw ApiException(
                    "scriptId ${manifest.scriptId} does not exist on the backend; " +
                        "remove it from tribot-script.json to create the script by name"
                )
            }
            checkZipSize(module.zipFile, "script creation")
            logger.lifecycle("[${module.name}] \"${manifest.name}\" not found, creating it")
            val created = client.createScript(manifest, env.authorUserId)
            // The source goes through the same upload-and-poll path as updates so a
            // first release is verified end to end instead of fire-and-forget.
            logger.lifecycle("[${module.name}] created script id ${created.id}, uploading initial source")
            publishSource(client, created.id, manifest, module)
            return ModuleResult(
                module.name, manifest.name, Status.RELEASED,
                "created script id ${created.id} and published version ${manifest.version}",
            )
        }

        val actions = mutableListOf<String>()
        if (metadataDiffers(manifest, remote)) {
            logger.lifecycle("[${module.name}] syncing metadata for script id ${remote.id}")
            client.updateMetadata(remote.id, manifest)
            actions += "metadata synced"
        }
        // Comparing versions is what lets the nightly run skip untouched scripts:
        // contributors bump the manifest version when they want a new source release.
        if (manifest.version != remote.version) {
            checkZipSize(module.zipFile, "source upload")
            logger.lifecycle(
                "[${module.name}] uploading source for script id ${remote.id} " +
                    "(${remote.version ?: "unknown"} to ${manifest.version})"
            )
            publishSource(client, remote.id, manifest, module)
            actions += "source updated ${remote.version ?: "unknown"} to ${manifest.version}"
        }

        return if (actions.isEmpty()) {
            ModuleResult(module.name, manifest.name, Status.SKIPPED, "up to date at version ${manifest.version}")
        } else {
            ModuleResult(module.name, manifest.name, Status.RELEASED, actions.joinToString("; "))
        }
    }

    private fun resolveRemote(client: TribotApiClient, manifest: ScriptManifest, authorUserId: Long): RemoteScript? {
        val scriptId = manifest.scriptId
        if (scriptId != null) return client.getScript(scriptId)
        return client.findScriptByName(manifest.name, authorUserId)
    }

    private fun metadataDiffers(manifest: ScriptManifest, remote: RemoteScript): Boolean {
        // Unknown remote values (a null categories column, or a malformed response)
        // count as differing because syncing is idempotent and cheap, while skipping
        // could leave stale metadata on the site.
        return remote.name != manifest.name ||
            remote.description != manifest.description ||
            remote.isCommunity != manifest.isCommunity ||
            remote.categories?.toSet() != manifest.categories.toSet()
    }

    private fun publishSource(client: TribotApiClient, scriptId: Long, manifest: ScriptManifest, module: ReleaseModule) {
        client.uploadSource(scriptId, manifest.version, module.zipFile.name, module.zipFile.readBytes())
        awaitSourceUpdate(client, scriptId, manifest.version, module.name)
    }

    private fun awaitSourceUpdate(client: TribotApiClient, scriptId: Long, version: String, module: String) {
        val deadline = System.nanoTime() + POLL_TIMEOUT.toNanos()
        var lastStatus = "submitted"
        while (true) {
            // submit-update returns no queue id, so the newest matching queue entry
            // for this script and version is polled instead.
            val update = client.getLatestSourceUpdate(scriptId, version)
            if (update != null) {
                lastStatus = update.status
                when (update.status) {
                    "success" -> return
                    // The queue's results field is raw compile-server output and these
                    // logs are public, so failures point at the queue entry instead of
                    // quoting it; the output is available in the dashboard or database.
                    "failed" -> throw ApiException(
                        "source update for version $version failed compilation; " +
                            "compiler output is in script update queue entry ${update.id}"
                    )
                }
            }
            if (System.nanoTime() >= deadline) {
                throw ApiException(
                    "timed out after ${POLL_TIMEOUT.toMinutes()} minutes waiting for the version $version " +
                        "source update of script $scriptId (last status: $lastStatus)"
                )
            }
            logger.lifecycle("[$module] source update for $version is \"$lastStatus\", polling again in ${POLL_INTERVAL.seconds}s")
            Thread.sleep(POLL_INTERVAL.toMillis())
        }
    }

    private fun checkZipSize(zip: File, action: String) {
        if (zip.length() > MAX_UPLOAD_MB * 1024L * 1024L) {
            throw GradleException(
                "${zip.name} is ${zip.length() / (1024 * 1024)} MB, over the backend's $MAX_UPLOAD_MB MB limit for $action"
            )
        }
    }

    private fun printSummary(results: List<ModuleResult>, dry: Boolean) {
        logger.lifecycle("")
        logger.lifecycle(if (dry) "releaseScripts dry-run summary:" else "releaseScripts summary:")
        results.forEach { result ->
            val script = result.script?.let { " (\"$it\")" } ?: ""
            logger.lifecycle("  [${result.status.label}] ${result.module}$script: ${result.detail}")
        }
        val counts = Status.entries
            .map { status -> status to results.count { it.status == status } }
            .filter { it.second > 0 }
            .joinToString(", ") { "${it.second} ${it.first.label}" }
        logger.lifecycle("  $counts")
    }

    private fun appendGithubStepSummary(results: List<ModuleResult>, dry: Boolean) {
        val path = System.getenv("GITHUB_STEP_SUMMARY")?.takeIf { it.isNotBlank() } ?: return
        val markdown = buildString {
            appendLine(if (dry) "## releaseScripts (dry run)" else "## releaseScripts")
            appendLine()
            appendLine("| Module | Script | Result | Detail |")
            appendLine("| --- | --- | --- | --- |")
            results.forEach {
                appendLine("| ${it.module} | ${mdCell(it.script ?: "")} | ${it.status.label} | ${mdCell(it.detail)} |")
            }
            appendLine()
        }
        try {
            File(path).appendText(markdown)
        } catch (e: Exception) {
            // A broken summary file should never turn a successful release into a failure.
            logger.warn("Could not append to GITHUB_STEP_SUMMARY: ${e.message}")
        }
    }

    private fun mdCell(text: String) = text.replace("|", "\\|").replace("\n", " ")

    private companion object {
        val POLL_TIMEOUT: Duration = Duration.ofMinutes(10)
        val POLL_INTERVAL: Duration = Duration.ofSeconds(10)

        // The admin endpoints reject multipart files over 25 MB; checking before the
        // upload gives a clearer error than the backend's generic 400.
        const val MAX_UPLOAD_MB = 25
    }
}
