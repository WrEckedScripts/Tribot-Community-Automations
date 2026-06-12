package tribot.release

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.net.URI
import java.nio.charset.StandardCharsets

/**
 * Exercises the client against a local stub server so the wire format (auth header,
 * query parameter casing, multipart encoding, JSON parsing) is verified without
 * touching the real backend. The backend deserializes query structs with serde's
 * rename_all = "camelCase", so the exact query strings asserted here are load-bearing:
 * a wrongly cased parameter is silently ignored server-side, not rejected.
 */
class TribotApiClientTest {

    private val apiKey = "test-key-do-not-log"
    private lateinit var server: HttpServer
    private lateinit var client: TribotApiClient

    private var lastAuth: String? = null
    private var lastContentType: String? = null
    private var lastBody: ByteArray = ByteArray(0)
    private val requestUris = mutableListOf<URI>()

    @BeforeEach
    fun start() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.start()
        client = TribotApiClient("http://127.0.0.1:${server.address.port}/", apiKey)
    }

    @AfterEach
    fun stop() = server.stop(0)

    private fun stub(path: String, status: Int, response: String) =
        stub(path) { status to response }

    private fun stub(path: String, handler: (URI) -> Pair<Int, String>) {
        server.createContext(path) { exchange ->
            lastAuth = exchange.requestHeaders.getFirst("Authorization")
            lastContentType = exchange.requestHeaders.getFirst("Content-Type")
            lastBody = exchange.requestBody.readBytes()
            requestUris += exchange.requestURI
            val (status, response) = handler(exchange.requestURI)
            respond(exchange, status, response)
        }
    }

    private fun respond(exchange: HttpExchange, status: Int, response: String) {
        val bytes = response.toByteArray(StandardCharsets.UTF_8)
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    private fun scriptJson(id: Long, name: String) =
        """{"id":$id,"name":"${name.replace("\"", "\\\"")}","description":"d","version":"1.0.0",
            "isCommunity":true,"categories":["Mining"],"authorUserId":3}"""

    @Test
    fun `find by name sends camelCase author scoping and the raw key after Basic`() {
        stub(
            "/api/internal/scripts", 200,
            """{"scripts":[${scriptJson(7, "Cam Torum Miner")}],"total":1,"page":1,"limit":100}""",
        )
        val script = client.findScriptByName("Cam Torum Miner", 3)
        assertEquals("Basic $apiKey", lastAuth)
        // author_user_id would be silently dropped by the backend's camelCase serde,
        // leaving the listing unscoped across all authors.
        assertEquals("name=Cam+Torum+Miner&authorUserId=3&page=1&limit=100", requestUris.single().rawQuery)
        assertEquals(RemoteScript(7, "Cam Torum Miner", "d", "1.0.0", true, listOf("Mining")), script)
    }

    @Test
    fun `find by name pages until the exact match shows up`() {
        // The name filter is a partial match server-side, so a page can fill up with
        // similarly named scripts while the exact match sits on a later page.
        stub("/api/internal/scripts") { uri ->
            val page = Regex("page=(\\d+)").find(uri.rawQuery)!!.groupValues[1].toInt()
            val scripts = when (page) {
                1 -> (1..100).joinToString(",") { scriptJson(it.toLong(), "Cam Torum Miner Pro $it") }
                else -> scriptJson(200, "Cam Torum Miner")
            }
            200 to """{"scripts":[$scripts],"total":101,"page":$page,"limit":100}"""
        }
        val script = client.findScriptByName("Cam Torum Miner", 3)
        assertEquals(200L, script?.id)
        assertEquals(2, requestUris.size)
        assertTrue(requestUris[1].rawQuery.contains("page=2"), requestUris[1].rawQuery)
    }

    @Test
    fun `find by name returns null once pages are exhausted without an exact match`() {
        stub(
            "/api/internal/scripts", 200,
            """{"scripts":[${scriptJson(7, "Cam Torum Miner Pro")}],"total":1,"page":1,"limit":100}""",
        )
        assertNull(client.findScriptByName("Cam Torum Miner", 3))
        assertEquals(1, requestUris.size)
    }

    @Test
    fun `create posts multipart with a placeholder version and no file part`() {
        stub("/api/internal/scripts", 200, """{"id":12,"name":"New Script"}""")
        val manifest = ScriptManifest("New Script", "desc", listOf("Mining", "Tools"), "1.2.3", true, null)

        val created = client.createScript(manifest, 3)

        assertEquals(12L, created.id)
        val boundary = lastContentType!!.substringAfter("multipart/form-data; boundary=")
        val body = lastBody.toString(StandardCharsets.ISO_8859_1)
        assertTrue(body.startsWith("--$boundary\r\n"), "body must open with the boundary")
        assertTrue(body.endsWith("--$boundary--\r\n"), "body must close the final boundary")
        assertTrue(body.contains("name=\"name\"\r\n\r\nNew Script\r\n"))
        assertTrue(body.contains("name=\"description\"\r\n\r\ndesc\r\n"))
        assertTrue(body.contains("name=\"isCommunity\"\r\n\r\ntrue\r\n"))
        assertTrue(body.contains("name=\"categories\"\r\n\r\nMining,Tools\r\n"))
        assertTrue(body.contains("name=\"authorUserId\"\r\n\r\n3\r\n"))
        // The manifest version must only reach the backend via a successful source
        // upload; create claims the placeholder so failed uploads get retried.
        assertTrue(body.contains("name=\"version\"\r\n\r\n${TribotApiClient.PLACEHOLDER_VERSION}\r\n"))
        assertFalse(body.contains("1.2.3"), "create must never send the manifest version")
        assertFalse(body.contains("filename="), "create must never send a file part")
    }

    @Test
    fun `update metadata puts json without a version field`() {
        stub("/api/internal/scripts/7", 200, """{"id":7}""")
        client.updateMetadata(7, ScriptManifest("N", "D", listOf("Mining"), "2.0.0", true, null))
        val body = lastBody.toString(StandardCharsets.UTF_8)
        assertEquals("application/json", lastContentType)
        assertTrue(body.contains("\"categories\":[\"Mining\"]"), body)
        assertFalse(body.contains("\"version\""), "version must only move via source uploads")
    }

    @Test
    fun `get script returns the full record and null on 404`() {
        stub("/api/internal/scripts/7", 200, scriptJson(7, "Cam Torum Miner"))
        stub("/api/internal/scripts/99", 404, """{"error":"not found"}""")
        assertEquals(
            RemoteScript(7, "Cam Torum Miner", "d", "1.0.0", true, listOf("Mining")),
            client.getScript(7),
        )
        assertNull(client.getScript(99))
    }

    @Test
    fun `upload source posts version and file to submit-update without a userId`() {
        stub("/api/internal/scripts/7/submit-update", 200, """{"success":true,"message":"Update submitted"}""")
        val zip = byteArrayOf(0x50, 0x4B, 0x03, 0x04, 0xFF.toByte())

        client.uploadSource(7, "1.0.1", "x.zip", zip)

        val boundary = lastContentType!!.substringAfter("multipart/form-data; boundary=")
        val body = lastBody.toString(StandardCharsets.ISO_8859_1)
        assertTrue(body.startsWith("--$boundary\r\n"), "body must open with the boundary")
        assertTrue(body.endsWith("--$boundary--\r\n"), "body must close the final boundary")
        assertTrue(body.contains("name=\"version\"\r\n\r\n1.0.1\r\n"))
        assertTrue(body.contains("name=\"file\"; filename=\"x.zip\"\r\nContent-Type: application/zip\r\n\r\n"))
        assertTrue(body.contains(zip.toString(StandardCharsets.ISO_8859_1)))
        // Omitting userId makes the backend default it to the script author.
        assertFalse(body.contains("userId"), "userId must be omitted")
    }

    @Test
    fun `polling picks the newest queue entry matching the version`() {
        stub(
            "/api/internal/script-updates", 200,
            """{"updates":[
                {"id":556,"scriptId":7,"scriptName":"X","userId":3,"version":"1.0.1",
                 "submitTime":"2026-06-12T00:00:02Z","status":"processing","results":null},
                {"id":555,"scriptId":7,"scriptName":"X","userId":3,"version":"1.0.1",
                 "submitTime":"2026-06-12T00:00:01Z","status":"failed","results":"compile error"},
                {"id":554,"scriptId":7,"scriptName":"X","userId":3,"version":"1.0.0",
                 "submitTime":"2026-06-12T00:00:00Z","status":"success","results":null}
            ],"total":3,"page":1,"limit":100}""",
        )

        val update = client.getLatestSourceUpdate(7, "1.0.1")
        assertEquals("scriptId=7&page=1&limit=100", requestUris.first().rawQuery)
        assertEquals(ScriptUpdate(556, "1.0.1", "processing"), update)
        // Entries for other versions must never satisfy or fail the poll.
        assertNull(client.getLatestSourceUpdate(7, "9.9.9"))
    }

    @Test
    fun `error messages carry the response body but never the api key`() {
        stub("/api/internal/scripts", 500, """{"error":"boom"}""")
        val e = assertThrows(ApiException::class.java) { client.findScriptByName("x", 1) }
        val message = e.message.orEmpty()
        assertTrue(message.contains("HTTP 500"), message)
        assertTrue(message.contains("boom"), message)
        assertFalse(message.contains(apiKey), "api key must never appear in errors")
    }

    @Test
    fun `error messages scrub the api host from response bodies`() {
        // A proxy or load balancer error page can echo the upstream URL; the base URL
        // is a CI secret while workflow logs are public.
        val base = "http://127.0.0.1:${server.address.port}"
        stub("/api/internal/scripts/7", 502, "upstream $base/api/internal/scripts/7 unavailable via 127.0.0.1")
        val e = assertThrows(ApiException::class.java) { client.getScript(7) }
        val message = e.message.orEmpty()
        assertFalse(message.contains("127.0.0.1"), message)
        assertTrue(message.contains("<api-host>"), message)
    }

    @Test
    fun `network errors never reveal the api host`() {
        // JDK exceptions embed the hostname for DNS failures, and GitHub's masking
        // only matches the full secret string including the scheme.
        val host = "tribot-release-test-host.invalid"
        val unreachable = TribotApiClient("https://$host", apiKey)
        val e = assertThrows(ApiException::class.java) { unreachable.getScript(1) }
        assertFalse(e.message.orEmpty().contains(host), e.message)
    }
}
