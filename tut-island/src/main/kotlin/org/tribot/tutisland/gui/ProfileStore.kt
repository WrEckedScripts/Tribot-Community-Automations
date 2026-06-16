package org.tribot.tutisland.gui

import org.tribot.util.Util
import java.io.File
import java.util.Properties

class ProfileStore(
    private val profileDir: File = File(Util.getWorkingDirectory(), "CrazyDavy/TutIsland/GUIProfiles")
) {
    init {
        profileDir.mkdirs()
    }

    fun listProfiles(): List<String> =
        profileDir
            .listFiles { file -> file.isFile && file.extension == "properties" }
            ?.map { it.nameWithoutExtension }
            ?.sorted()
            ?: emptyList()

    fun load(name: String): UserSettings? =
        runCatching {
            val file = profileFile(name)
            if (!file.exists()) {
                return null
            }

            val defaults = UserSettings()
            val properties = Properties()
            file.inputStream().use(properties::load)

            UserSettings(
                ironmanMode = properties.getProperty("ironmanMode", defaults.ironmanMode),
                walkLocation = properties.getProperty("walkLocation", defaults.walkLocation)
            )
        }.getOrNull()

    fun save(name: String, settings: UserSettings): Boolean =
        runCatching {
            profileDir.mkdirs()
            val properties = Properties().apply {
                setProperty("ironmanMode", settings.ironmanMode)
                setProperty("walkLocation", settings.walkLocation)
            }

            profileFile(name).outputStream().use { output ->
                properties.store(output, "Tutorial Island profile")
            }
            true
        }.getOrDefault(false)

    fun delete(name: String): Boolean {
        val file = profileFile(name)
        return file.exists() && file.delete()
    }

    private fun profileFile(name: String): File =
        File(profileDir, "${sanitizedProfileName(name)}.properties")

    private fun sanitizedProfileName(name: String): String =
        name.trim()
            .ifBlank { "default" }
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
}
