package org.tribot.wrblastpumper.gui

import org.tribot.script.sdk.util.ScriptSettings

//TODO wire in validation errors / log them as debug/warn.
class PumperProfileStore {
    private val storage: ScriptSettings
        get() = ScriptSettings.getDefault()

    fun listProfiles(): List<String> = runCatching {
        storage.saveNames.sortedWith(String.CASE_INSENSITIVE_ORDER)
    }.getOrDefault(emptyList())

    fun load(name: String): PumperSettings? = runCatching {
        val requestedName = normalizedName(name)
        if (!isValidName(requestedName)) return null

        val storedName = listProfiles().firstOrNull {
            it.equals(requestedName, ignoreCase = true)
        } ?: requestedName

        storage.load(storedName, PumperSettings::class.java).orElse(null)
    }.getOrNull()

    fun save(name: String, settings: PumperSettings): Boolean = runCatching {
        val profileName = normalizedName(name)
        isValidName(profileName) && storage.save(profileName, settings.copy())
    }.getOrDefault(false)

    fun isValidName(name: String): Boolean {
        val normalized = normalizedName(name)
        return normalized.length in 1..50 &&
                normalized.first().isLetterOrDigit() &&
                normalized.all {
                    it.isLetterOrDigit() || it == ' ' || it == '-' || it == '_' || it == '.'
                }
    }

    fun normalizedName(name: String): String {
        val trimmed = name.trim()
        return if (trimmed.endsWith(".json", ignoreCase = true)) {
            trimmed.dropLast(".json".length).trim()
        } else {
            trimmed
        }
    }

    companion object {
        const val LAST_RUN_PROFILE = "lastrun"
    }
}
