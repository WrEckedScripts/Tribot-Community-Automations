package tribot.release

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

class ApiException(message: String) : RuntimeException(message)

/**
 * A script as the backend's admin endpoints report it. `categories` is nullable in the
 * database; the release logic treats a null as differing so the metadata sync still
 * happens. The other nullable fields only guard against malformed responses.
 */
data class RemoteScript(
    val id: Long,
    val name: String,
    val description: String?,
    val version: String?,
    val isCommunity: Boolean?,
    val categories: List<String>?,
)

/**
 * One entry of the script update queue, as listed by the admin endpoint. The backend
 * also returns a `results` field holding raw compile-server output; it is deliberately
 * not modeled here so it can never reach the public CI logs.
 */
data class ScriptUpdate(
    val id: Long,
    val version: String,
    val status: String,
)

/**
 * Thin client over the backend's admin endpoints. The scripter endpoints are
 * deliberately not used: they hard-check that the script author is the API key owner,
 * so releases under a different TRIBOT_AUTHOR_USER_ID would silently 404. Error
 * messages and exceptions deliberately never include request headers so the API key
 * cannot leak into CI logs; only the method, path, status code, and response body
 * are surfaced, and the API host is scrubbed from all of it because the base URL is
 * a CI secret while the workflow logs are public.
 */
class TribotApiClient(baseUrl: String, private val apiKey: String) {

    private val baseUrl = baseUrl.trimEnd('/')

    // GitHub only masks exact secret strings, so the bare host inside a JDK network
    // exception ("UnknownHostException: api.example.org") or a proxy error page would
    // slip past masking of the full TRIBOT_API_BASE_URL value.
    private val hostSecrets = listOfNotNull(this.baseUrl, URI.create(this.baseUrl).host)

    private val gson = Gson()
    private val http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    /** Returns null when the script id does not exist. */
    fun getScript(id: Long): RemoteScript? {
        val path = "/api/internal/scripts/$id"
        val response = send("GET", path, null, null)
        if (response.statusCode() == 404) return null
        val body = expectSuccess(response, "GET", path)
        return toRemoteScript(parseObject(body, "GET", path), "GET", path)
    }

    /**
     * Finds the author's script whose name matches exactly. The backend's name filter
     * is a case-insensitive partial match, so the exact comparison has to happen
     * client-side, and the match can sit beyond the first page (the server caps a
     * page at 100), so all pages are walked before concluding the script is missing.
     * Missing a match here would make the nightly run create a duplicate script.
     */
    fun findScriptByName(name: String, authorUserId: Long): RemoteScript? {
        var page = 1L
        var seen = 0L
        while (true) {
            val result = listScriptsPage(name, authorUserId, page)
            result.scripts.firstOrNull { it.name == name }?.let { return it }
            seen += result.scripts.size
            // Counting received items instead of assuming the server's page size keeps
            // termination correct if the backend ever lowers its limit clamp; stopping
            // early would make the nightly run create a duplicate script. The
            // empty-page guard covers totals shrinking while paginating.
            if (result.scripts.isEmpty() || seen >= result.total) return null
            page++
        }
    }

    fun createScript(manifest: ScriptManifest, authorUserId: Long): RemoteScript {
        // No file part, and a placeholder version instead of the manifest's: create
        // would set scripts.version immediately while only enqueueing the zip, so a
        // failed queue run would leave the site claiming a version whose source never
        // published and later runs would skip the module as up to date. With the
        // placeholder, the remote version only moves when a source upload succeeds,
        // so failed uploads are retried naturally.
        val multipart = MultipartBody.Builder()
            .field("name", manifest.name)
            .field("description", manifest.description)
            .field("version", PLACEHOLDER_VERSION)
            .field("isCommunity", manifest.isCommunity.toString())
            .field("categories", manifest.categories.joinToString(","))
            .field("authorUserId", authorUserId.toString())
            .build()
        val path = "/api/internal/scripts"
        val response = send(
            "POST", path, multipart.contentType,
            HttpRequest.BodyPublishers.ofByteArray(multipart.bytes),
        )
        val body = expectSuccess(response, "POST", path)
        return toRemoteScript(parseObject(body, "POST", path), "POST", path)
    }

    fun updateMetadata(id: Long, manifest: ScriptManifest) {
        // Version is intentionally not part of the metadata sync: it only moves when a
        // source upload succeeds, otherwise the site would claim a version whose
        // source was never published.
        val payload = JsonObject().apply {
            addProperty("name", manifest.name)
            addProperty("description", manifest.description)
            addProperty("isCommunity", manifest.isCommunity)
            add("categories", gson.toJsonTree(manifest.categories))
        }
        val path = "/api/internal/scripts/$id"
        val response = send(
            "PUT", path, "application/json",
            HttpRequest.BodyPublishers.ofString(gson.toJson(payload), StandardCharsets.UTF_8),
        )
        expectSuccess(response, "PUT", path)
    }

    fun uploadSource(id: Long, version: String, zipName: String, zip: ByteArray) {
        // userId is omitted on purpose so the backend defaults it to the script's
        // author rather than the API key owner.
        val multipart = MultipartBody.Builder()
            .field("version", version)
            .file("file", zipName, "application/zip", zip)
            .build()
        val path = "/api/internal/scripts/$id/submit-update"
        val response = send(
            "POST", path, multipart.contentType,
            HttpRequest.BodyPublishers.ofByteArray(multipart.bytes),
        )
        expectSuccess(response, "POST", path)
    }

    /**
     * The submit-update response carries no queue id, so the newest queue entry for
     * this script and version is the only handle on the upload's fate. Returns null
     * when no matching entry is visible yet.
     */
    fun getLatestSourceUpdate(scriptId: Long, version: String): ScriptUpdate? {
        val path = "/api/internal/script-updates?scriptId=$scriptId&page=1&limit=$PAGE_LIMIT"
        val body = expectSuccess(send("GET", path, null, null), "GET", path)
        val updates = parseObject(body, "GET", path)
            .get("updates")?.takeIf { it.isJsonArray }?.asJsonArray ?: return null
        return updates
            .mapNotNull { it.takeIf { e -> e.isJsonObject }?.asJsonObject }
            .mapNotNull { toScriptUpdate(it) }
            .filter { it.version == version }
            .maxByOrNull { it.id }
    }

    private data class ScriptListPage(val scripts: List<RemoteScript>, val total: Long)

    private fun listScriptsPage(name: String, authorUserId: Long, page: Long): ScriptListPage {
        // The backend deserializes the query with rename_all = "camelCase", so the
        // author filter must be authorUserId; author_user_id would be silently
        // ignored and the listing would be unscoped.
        val path = "/api/internal/scripts?name=${encode(name)}&authorUserId=$authorUserId&page=$page&limit=$PAGE_LIMIT"
        val body = expectSuccess(send("GET", path, null, null), "GET", path)
        val obj = parseObject(body, "GET", path)
        val scripts = obj.getAsJsonArray("scripts")
            ?.map { toRemoteScript(it.asJsonObject, "GET", path) }
            ?: emptyList()
        val total = obj.get("total")?.takeIf { it.isJsonPrimitive }?.asLong ?: scripts.size.toLong()
        return ScriptListPage(scripts, total)
    }

    private fun send(
        method: String,
        path: String,
        contentType: String?,
        body: HttpRequest.BodyPublisher?,
    ): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI.create(baseUrl + path))
            // Uploads can be tens of MB; give slow links room before declaring failure.
            .timeout(Duration.ofMinutes(5))
            // The backend expects the raw key after "Basic", not a base64 credentials
            // pair; it hashes and compares the value server-side.
            .header("Authorization", "Basic $apiKey")
            .apply { if (contentType != null) header("Content-Type", contentType) }
            .method(method, body ?: HttpRequest.BodyPublishers.noBody())
            .build()
        return try {
            http.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: IOException) {
            // Rewrap so no stack frame or attached request object (with its auth
            // header) can ride along into Gradle's error output.
            throw apiError("$method $path failed: ${e.javaClass.simpleName}: ${e.message}")
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw apiError("$method $path interrupted")
        }
    }

    private fun expectSuccess(response: HttpResponse<String>, method: String, path: String): String {
        if (response.statusCode() !in 200..299) {
            throw apiError("$method $path returned HTTP ${response.statusCode()}: ${truncate(response.body())}")
        }
        return response.body()
    }

    private fun parseObject(body: String, method: String, path: String): JsonObject {
        val element = try {
            JsonParser.parseString(body)
        } catch (e: JsonSyntaxException) {
            throw apiError("$method $path returned a non-JSON response: ${truncate(body)}")
        }
        if (!element.isJsonObject) {
            throw apiError("$method $path returned unexpected JSON: ${truncate(body)}")
        }
        return element.asJsonObject
    }

    private fun toRemoteScript(obj: JsonObject, method: String, path: String): RemoteScript {
        val id = obj.get("id")?.takeIf { it.isJsonPrimitive }?.asLong
            ?: throw apiError("$method $path returned a script without an id: ${truncate(obj.toString())}")
        return RemoteScript(
            id = id,
            name = obj.get("name")?.takeIf { it.isJsonPrimitive }?.asString ?: "",
            description = obj.get("description")?.takeIf { it.isJsonPrimitive }?.asString,
            version = obj.get("version")?.takeIf { it.isJsonPrimitive }?.asString,
            isCommunity = obj.get("isCommunity")
                ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isBoolean }?.asBoolean,
            categories = obj.get("categories")?.takeIf { it.isJsonArray }
                ?.asJsonArray?.mapNotNull { c -> c.takeIf { it.isJsonPrimitive }?.asString },
        )
    }

    private fun toScriptUpdate(obj: JsonObject): ScriptUpdate? {
        val id = obj.get("id")?.takeIf { it.isJsonPrimitive }?.asLong ?: return null
        val status = obj.get("status")?.takeIf { it.isJsonPrimitive }?.asString ?: return null
        return ScriptUpdate(
            id = id,
            version = obj.get("version")?.takeIf { it.isJsonPrimitive }?.asString ?: "",
            status = status,
        )
    }

    private fun apiError(message: String) =
        ApiException(hostSecrets.fold(message) { text, secret -> text.replace(secret, "<api-host>") })

    private fun encode(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8)

    private fun truncate(body: String?): String {
        val text = body?.trim().orEmpty().ifEmpty { "<empty body>" }
        return if (text.length <= 500) text else text.take(500) + "... (truncated)"
    }

    companion object {
        // Server-side cap on a listing page; asking for more is silently clamped.
        private const val PAGE_LIMIT = 100L

        // See createScript for why the manifest version is never sent at create time.
        const val PLACEHOLDER_VERSION = "0.0.0"
    }
}
