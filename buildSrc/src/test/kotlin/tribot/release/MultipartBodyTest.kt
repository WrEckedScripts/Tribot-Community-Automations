package tribot.release

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class MultipartBodyTest {

    @Test
    fun `encodes fields and a binary file part with exact delimiters`() {
        // Includes bytes that are invalid UTF-8 to prove the binary part is written
        // verbatim and never round-tripped through a string.
        val fileBytes = byteArrayOf(0x50, 0x4B, 0x03, 0x04, 0x00, 0xFF.toByte(), 0x0D, 0x0A)
        val body = MultipartBody.Builder()
            .field("name", "Cam Torum Miner")
            .field("isCommunity", "true")
            .file("file", "cam-torum-miner-sources.zip", "application/zip", fileBytes)
            .build()

        val b = body.boundary
        assertEquals("multipart/form-data; boundary=$b", body.contentType)

        val expected = ("--$b\r\n" +
            "Content-Disposition: form-data; name=\"name\"\r\n" +
            "\r\n" +
            "Cam Torum Miner\r\n" +
            "--$b\r\n" +
            "Content-Disposition: form-data; name=\"isCommunity\"\r\n" +
            "\r\n" +
            "true\r\n" +
            "--$b\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"cam-torum-miner-sources.zip\"\r\n" +
            "Content-Type: application/zip\r\n" +
            "\r\n").toByteArray(StandardCharsets.US_ASCII) +
            fileBytes +
            "\r\n--$b--\r\n".toByteArray(StandardCharsets.US_ASCII)

        assertArrayEquals(expected, body.bytes)
    }

    @Test
    fun `field values are written as utf8`() {
        val body = MultipartBody.Builder()
            .field("description", "fiskér")
            .build()
        val text = body.bytes.toString(StandardCharsets.UTF_8)
        assertEquals(true, text.contains("fiskér"))
    }

    @Test
    fun `quotes and line breaks cannot escape header parameters`() {
        val body = MultipartBody.Builder()
            .file("file", "evil\"\r\nX-Injected: yes.zip", "application/zip", ByteArray(0))
            .build()
        val text = body.bytes.toString(StandardCharsets.ISO_8859_1)
        // The payload may survive inside the quoted parameter, but it must not be able
        // to terminate the quote or start a new header line.
        assertFalse(text.contains("\r\nX-Injected"))
        assertEquals(true, text.contains("filename=\"evil\\\"X-Injected: yes.zip\""))
    }
}
