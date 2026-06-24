package tribot.release

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Minimal multipart/form-data encoder (RFC 7578). The JDK's java.net.http client has
 * no built-in multipart support and pulling in an HTTP library just for this is not
 * worth a new build-logic dependency.
 */
class MultipartBody private constructor(val boundary: String, val bytes: ByteArray) {

    val contentType: String
        get() = "multipart/form-data; boundary=$boundary"

    class Builder {
        // A random boundary makes accidental collision with part content practically
        // impossible without having to scan the binary payload for the delimiter.
        private val boundary = "tribot-release-" + UUID.randomUUID()
        private val out = ByteArrayOutputStream()

        fun field(name: String, value: String): Builder {
            writeAscii("--$boundary\r\n")
            writeAscii("Content-Disposition: form-data; name=\"${escape(name)}\"\r\n")
            writeAscii("\r\n")
            // Field values may contain non-ASCII characters (script descriptions);
            // headers stay ASCII but the value bytes go through as UTF-8.
            out.write(value.toByteArray(StandardCharsets.UTF_8))
            writeAscii("\r\n")
            return this
        }

        fun file(name: String, filename: String, contentType: String, content: ByteArray): Builder {
            writeAscii("--$boundary\r\n")
            writeAscii(
                "Content-Disposition: form-data; name=\"${escape(name)}\"; filename=\"${escape(filename)}\"\r\n"
            )
            writeAscii("Content-Type: $contentType\r\n")
            writeAscii("\r\n")
            out.write(content)
            writeAscii("\r\n")
            return this
        }

        fun build(): MultipartBody {
            writeAscii("--$boundary--\r\n")
            return MultipartBody(boundary, out.toByteArray())
        }

        private fun writeAscii(text: String) = out.write(text.toByteArray(StandardCharsets.US_ASCII))

        // Raw quotes or line breaks inside a header parameter would corrupt the part
        // headers, so neutralize them rather than trust callers.
        private fun escape(value: String) = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "")
            .replace("\n", "")
    }
}
