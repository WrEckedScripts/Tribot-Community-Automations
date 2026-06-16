package org.tribot.gemstoneminer.gui

import org.tribot.util.Util
import java.io.File
import java.util.*

class ProfileStore(
    private val profileDir: File = File(Util.getWorkingDirectory(), "CrazyDavy/GemStoneMiner/GUIProfiles")
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

            val properties = Properties()
            file.inputStream().use(properties::load)
            val defaults = UserSettings()

            val oldMicroBreaksEnabled = properties.getProperty("microBreaksEnabled")?.toBooleanStrictOrNull()
            val loadedBreakEveryMinutes = properties.getProperty(
                "breakEveryMinutes",
                defaults.breakEveryMinutes.toString()
            ).toIntOrNull() ?: defaults.breakEveryMinutes
            val loadedBreakLengthSeconds = properties.getProperty(
                "breakLengthSeconds",
                defaults.breakLengthSeconds.toString()
            ).toIntOrNull() ?: defaults.breakLengthSeconds

            val oldWorldHopEnabled = properties.getProperty("worldHopEnabled")?.toBooleanStrictOrNull()
            val loadedWorldHopMinutes = properties.getProperty(
                "worldHopMinutes",
                defaults.worldHopMinutes.toString()
            ).toIntOrNull() ?: defaults.worldHopMinutes

            UserSettings(
                mineLevel = properties.getProperty("mineLevel", defaults.mineLevel),
                stopAtMiningLevel = properties.getProperty(
                    "stopAtMiningLevel",
                    defaults.stopAtMiningLevel.toString()
                ).toIntOrNull() ?: defaults.stopAtMiningLevel,
                stopAfterGemRocks = properties.getProperty(
                    "stopAfterGemRocks",
                    defaults.stopAfterGemRocks.toString()
                ).toIntOrNull() ?: defaults.stopAfterGemRocks,
                breakEveryMinutes = if (oldMicroBreaksEnabled == false) 0 else loadedBreakEveryMinutes,
                breakLengthSeconds = if (oldMicroBreaksEnabled == false) 0 else loadedBreakLengthSeconds,
                logoutDuringBreak = properties.getProperty(
                    "logoutDuringBreak",
                    defaults.logoutDuringBreak.toString()
                ).toBoolean(),
                worldHopMinutes = if (oldWorldHopEnabled == false) 0 else loadedWorldHopMinutes
            )
        }.getOrNull()

    fun save(name: String, settings: UserSettings): Boolean =
        runCatching {
            profileDir.mkdirs()
            val properties = Properties().apply {
                setProperty("mineLevel", settings.mineLevel)
                setProperty("stopAtMiningLevel", settings.stopAtMiningLevel.toString())
                setProperty("stopAfterGemRocks", settings.stopAfterGemRocks.toString())
                setProperty("breakEveryMinutes", settings.breakEveryMinutes.toString())
                setProperty("breakLengthSeconds", settings.breakLengthSeconds.toString())
                setProperty("logoutDuringBreak", settings.logoutDuringBreak.toString())
                setProperty("worldHopMinutes", settings.worldHopMinutes.toString())
            }

            profileFile(name).outputStream().use { output ->
                properties.store(output, "Gem Stone Miner profile")
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
