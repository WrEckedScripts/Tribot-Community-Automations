package org.tribot.tutisland.gui

import org.tribot.script.sdk.Log
import org.tribot.util.Util
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class ProfileStore(
    basePath: String = Path.of(
        Util.getWorkingDirectory().absolutePath,
        "CrazyDavy",
        "TutIsland",
        "GUIProfiles"
    ).toString()
) {
    private val baseDir = Path.of(basePath)

    init {
        runCatching {
            Files.createDirectories(baseDir)
        }.onFailure { error ->
            Log.warn("[TutIsland] Failed to create profile directory $baseDir: ${error.message}")
        }
    }

    fun listProfiles(): List<String> =
        runCatching {
            if (!Files.isDirectory(baseDir)) return@runCatching emptyList()

            Files.list(baseDir).use { paths ->
                paths
                    .filter { Files.isRegularFile(it) }
                    .filter { it.fileName.toString().endsWith(PROFILE_SUFFIX, ignoreCase = true) }
                    .map { it.fileName.toString().removeSuffix(PROFILE_SUFFIX) }
                    .toList()
                    .sortedWith(String.CASE_INSENSITIVE_ORDER)
            }
        }.getOrElse { emptyList() }

    fun save(name: String, settings: UserSettings): Boolean =
        runCatching {
            Files.createDirectories(baseDir)
            Files.newOutputStream(profilePath(name)).use { out ->
                Properties()
                    .apply {
                        setProperty("ironmanMode", settings.ironmanMode)
                        setProperty("walkLocation", settings.walkLocation)
                    }
                    .store(out, "Tut Island profile")
            }
            true
        }.getOrElse { error ->
            Log.warn("[TutIsland] Failed to save profile ${name.sanitizedName()}: ${error.message}")
            false
        }

    fun load(name: String): UserSettings? =
        runCatching {
            val path = profilePath(name)
            if (!Files.isRegularFile(path)) return@runCatching null

            val properties = Properties()
            Files.newInputStream(path).use { properties.load(it) }

            UserSettings(
                ironmanMode = properties.getProperty("ironmanMode", UserSettings().ironmanMode),
                walkLocation = properties.getProperty("walkLocation", UserSettings().walkLocation)
            )
        }.getOrElse { error ->
            Log.warn("[TutIsland] Failed to load profile ${name.sanitizedName()}: ${error.message}")
            null
        }.also { if (it == null) Log.info("[TutIsland] Profile not found: ${name.sanitizedName()}") }

    fun delete(name: String): Boolean =
        runCatching { Files.deleteIfExists(profilePath(name)) }.getOrDefault(false)

    fun exists(name: String): Boolean = listProfiles().any { it.equals(name.sanitizedName(), true) }

    private fun profilePath(name: String): Path =
        baseDir.resolve("${name.sanitizedName()}$PROFILE_SUFFIX")

    private fun String.sanitizedName(): String =
        trim()
            .removeSuffix(PROFILE_SUFFIX)
            .removeSuffix(".json")
            .replace(Regex("[^a-zA-Z0-9-_ ]"), "")
            .trim()
            .ifBlank { "default" }

    private companion object {
        const val PROFILE_SUFFIX = ".properties"
    }
}
