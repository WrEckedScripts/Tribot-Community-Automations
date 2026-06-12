package tribot.release

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class ScriptManifestTest {

    @TempDir
    lateinit var dir: Path

    private fun load(json: String): ScriptManifest {
        val file = File(dir.toFile(), "tribot-script.json").apply { writeText(json) }
        return ScriptManifest.load("test-module", file)
    }

    private fun loadError(json: String): String {
        val e = assertThrows(ManifestException::class.java) { load(json) }
        return e.message.orEmpty()
    }

    @Test
    fun `parses a full manifest`() {
        val manifest = load(
            """
            {
              "name": "Cam Torum Miner",
              "description": "Mines blessed bone shards and banks",
              "categories": ["Mining", "Money Making"],
              "version": "1.0.0",
              "isCommunity": false,
              "scriptId": 42
            }
            """
        )
        assertEquals("Cam Torum Miner", manifest.name)
        assertEquals(listOf("Mining", "Money Making"), manifest.categories)
        assertEquals("1.0.0", manifest.version)
        assertEquals(false, manifest.isCommunity)
        assertEquals(42L, manifest.scriptId)
    }

    @Test
    fun `applies defaults for optional fields`() {
        val manifest = load(
            """{"name": "X", "description": "Y", "categories": ["Mining"], "version": "1.0.0"}"""
        )
        assertEquals(true, manifest.isCommunity)
        assertNull(manifest.scriptId)
    }

    @Test
    fun `errors name the module and the missing field`() {
        val message = loadError("""{"name": "X", "categories": ["Mining"], "version": "1.0.0"}""")
        assertTrue(message.contains("test-module"), message)
        assertTrue(message.contains("\"description\""), message)
    }

    @Test
    fun `rejects categories outside the backend enum`() {
        val message = loadError(
            """{"name": "X", "description": "Y", "categories": ["mining"], "version": "1.0.0"}"""
        )
        assertTrue(message.contains("invalid categor"), message)
        assertTrue(message.contains("mining"), message)
    }

    @Test
    fun `rejects empty categories`() {
        val message = loadError(
            """{"name": "X", "description": "Y", "categories": [], "version": "1.0.0"}"""
        )
        assertTrue(message.contains("must not be empty"), message)
    }

    @Test
    fun `rejects versions over the backend limit`() {
        val message = loadError(
            """{"name": "X", "description": "Y", "categories": ["Mining"], "version": "1.0.0-really-long"}"""
        )
        assertTrue(message.contains("at most 15"), message)
    }

    @Test
    fun `rejects the reserved placeholder version`() {
        val message = loadError(
            """{"name": "X", "description": "Y", "categories": ["Mining"], "version": "0.0.0"}"""
        )
        assertTrue(message.contains("placeholder"), message)
    }

    @Test
    fun `rejects unknown fields to catch typos`() {
        val message = loadError(
            """{"name": "X", "description": "Y", "category": ["Mining"], "categories": ["Mining"], "version": "1.0.0"}"""
        )
        assertTrue(message.contains("category"), message)
    }

    @Test
    fun `rejects invalid json with a clear message`() {
        val message = loadError("{not json")
        assertTrue(message.contains("not valid JSON"), message)
    }
}
