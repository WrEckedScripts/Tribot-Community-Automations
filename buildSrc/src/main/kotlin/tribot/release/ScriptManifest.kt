package tribot.release

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import java.io.File

class ManifestException(message: String) : RuntimeException(message)

/**
 * A subproject's `tribot-script.json`. Presence of the file is what opts the module
 * into the website release pipeline, so validation errors must name the module and
 * field clearly: the person reading the failure is usually the contributor who just
 * added the manifest, not a maintainer.
 */
data class ScriptManifest(
    val name: String,
    val description: String,
    val categories: List<String>,
    val version: String,
    val isCommunity: Boolean,
    val scriptId: Long?,
) {
    companion object {
        // Must match the backend's category enum exactly; anything else is rejected
        // server-side, so catching it at manifest load gives a much better error.
        val ALLOWED_CATEGORIES = setOf(
            "Agility", "Combat", "Construction", "Cooking", "Crafting", "Farming",
            "Firemaking", "Fishing", "Fletching", "Herblore", "Hunter", "Magic",
            "Minigames", "Mining", "Money Making", "Prayer", "Questing",
            "Runecrafting", "Slayer", "Smithing", "Thieving", "Tools", "Woodcutting",
        )

        // Backend column limit; both the create and source-upload endpoints enforce it.
        const val MAX_VERSION_LENGTH = 15

        private val KNOWN_FIELDS =
            setOf("name", "description", "categories", "version", "isCommunity", "scriptId")

        fun load(module: String, file: File): ScriptManifest {
            val root = try {
                JsonParser.parseString(file.readText())
            } catch (e: JsonSyntaxException) {
                throw ManifestException("$module: tribot-script.json is not valid JSON: ${e.message}")
            }
            if (!root.isJsonObject) {
                throw ManifestException("$module: tribot-script.json must be a JSON object")
            }
            return parse(module, root.asJsonObject)
        }

        private fun parse(module: String, obj: JsonObject): ScriptManifest {
            // A typo like "category" would otherwise be silently ignored and the field
            // it was meant to set would fail as "missing", which is confusing.
            val unknown = obj.keySet() - KNOWN_FIELDS
            if (unknown.isNotEmpty()) {
                throw ManifestException(
                    "$module: unknown field(s) in tribot-script.json: " +
                        "${unknown.sorted().joinToString(", ")} (allowed: ${KNOWN_FIELDS.sorted().joinToString(", ")})"
                )
            }

            val name = requireString(module, obj, "name")
            val description = requireString(module, obj, "description")
            val version = requireString(module, obj, "version")
            if (version.length > MAX_VERSION_LENGTH) {
                throw ManifestException(
                    "$module: field \"version\" in tribot-script.json must be at most " +
                        "$MAX_VERSION_LENGTH characters, got ${version.length} (\"$version\")"
                )
            }
            // The release pipeline creates scripts at this placeholder version so a
            // failed first upload retries on the next run; a manifest using the same
            // value would make that failure look permanently up to date.
            if (version == TribotApiClient.PLACEHOLDER_VERSION) {
                throw ManifestException(
                    "$module: field \"version\" in tribot-script.json must not be " +
                        "\"${TribotApiClient.PLACEHOLDER_VERSION}\" (reserved as the pre-release placeholder)"
                )
            }

            val categories = parseCategories(module, obj)
            val isCommunity = parseIsCommunity(module, obj)
            val scriptId = parseScriptId(module, obj)

            return ScriptManifest(name, description, categories, version, isCommunity, scriptId)
        }

        private fun requireString(module: String, obj: JsonObject, field: String): String {
            val element = obj.get(field)
                ?: throw ManifestException("$module: missing required field \"$field\" in tribot-script.json")
            if (!element.isString()) {
                throw ManifestException("$module: field \"$field\" in tribot-script.json must be a string")
            }
            val value = element.asString.trim()
            if (value.isEmpty()) {
                throw ManifestException("$module: field \"$field\" in tribot-script.json must not be blank")
            }
            return value
        }

        private fun parseCategories(module: String, obj: JsonObject): List<String> {
            val element = obj.get("categories")
                ?: throw ManifestException("$module: missing required field \"categories\" in tribot-script.json")
            if (!element.isJsonArray) {
                throw ManifestException("$module: field \"categories\" in tribot-script.json must be an array of strings")
            }
            val values = element.asJsonArray.map {
                if (!it.isString()) {
                    throw ManifestException("$module: field \"categories\" in tribot-script.json must contain only strings")
                }
                it.asString.trim()
            }
            if (values.isEmpty()) {
                throw ManifestException("$module: field \"categories\" in tribot-script.json must not be empty")
            }
            val invalid = values.filter { it !in ALLOWED_CATEGORIES }
            if (invalid.isNotEmpty()) {
                throw ManifestException(
                    "$module: invalid categor${if (invalid.size == 1) "y" else "ies"} in tribot-script.json: " +
                        "${invalid.joinToString(", ")} (allowed: ${ALLOWED_CATEGORIES.sorted().joinToString(", ")})"
                )
            }
            return values
        }

        private fun parseIsCommunity(module: String, obj: JsonObject): Boolean {
            val element = obj.get("isCommunity") ?: return true
            if (!element.isJsonPrimitive || !element.asJsonPrimitive.isBoolean) {
                throw ManifestException("$module: field \"isCommunity\" in tribot-script.json must be a boolean")
            }
            return element.asBoolean
        }

        private fun parseScriptId(module: String, obj: JsonObject): Long? {
            val element = obj.get("scriptId") ?: return null
            if (!element.isJsonPrimitive || !element.asJsonPrimitive.isNumber) {
                throw ManifestException("$module: field \"scriptId\" in tribot-script.json must be a number")
            }
            val value = element.asLong
            if (value <= 0) {
                throw ManifestException("$module: field \"scriptId\" in tribot-script.json must be a positive id")
            }
            return value
        }

        private fun JsonElement.isString() = isJsonPrimitive && asJsonPrimitive.isString
    }
}
