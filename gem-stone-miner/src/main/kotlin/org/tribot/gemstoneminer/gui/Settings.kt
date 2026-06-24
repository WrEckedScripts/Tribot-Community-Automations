package org.tribot.gemstoneminer.gui

import org.tribot.community.commons.ScriptArgsHelper
import org.tribot.gemstoneminer.util.data.MineLevel

object Settings {
    var mineLevel: MineLevel = MineLevel.LOWER
    var stopAtMiningLevel: Int = 0
    var stopAfterGemRocks: Int = 0
    var breakEveryMinutes: Int = 0
    var breakLengthSeconds: Int = 0
    var logoutDuringBreak: Boolean = false
    var worldHopMinutes: Int = 0

    val microBreaksEnabled: Boolean
        get() = breakEveryMinutes > 0 && breakLengthSeconds > 0

    val worldHopEnabled: Boolean
        get() = worldHopMinutes > 0

    fun reset() {
        mineLevel = MineLevel.LOWER
        stopAtMiningLevel = 0
        stopAfterGemRocks = 0
        breakEveryMinutes = 0
        breakLengthSeconds = 0
        logoutDuringBreak = false
        worldHopMinutes = 0
    }

    fun loadArgs(): ArgsResult {
        reset()
        return applyArgs()
    }

    fun applyArgs(): ArgsResult {
        val invalidValues = mutableListOf<String>()

        ScriptArgsHelper.valueOf("level", "minelevel", "location", "area")?.let { value ->
            MineLevel.entries.matchArg(value)
                ?.let { mineLevel = it }
                ?: invalidValues.add("level=$value")
        }

        ScriptArgsHelper.valueOf("stopatlevel", "stoplevel", "stopmininglevel", "stopatmininglevel")?.let { value ->
            value.toIntInRange(0, 99)
                ?.let { stopAtMiningLevel = it }
                ?: invalidValues.add("stopAtMiningLevel=$value")
        }

        ScriptArgsHelper.valueOf("stopafterrocks", "stoprocks", "stopgemrocks", "stopaftergemrocks")?.let { value ->
            value.toIntInRange(0, 9999)
                ?.let { stopAfterGemRocks = it }
                ?: invalidValues.add("stopAfterGemRocks=$value")
        }

        ScriptArgsHelper.valueOf("microbreaks", "breaks", "usebreaks")?.let { value ->
            value.toBooleanArg()
                ?.let { enabled ->
                    if (!enabled) {
                        breakEveryMinutes = 0
                        breakLengthSeconds = 0
                    } else {
                        if (breakEveryMinutes <= 0) breakEveryMinutes = 15
                        if (breakLengthSeconds <= 0) breakLengthSeconds = 45
                    }
                }
                ?: invalidValues.add("microBreaks=$value")
        }

        ScriptArgsHelper.valueOf("breakevery", "breakinterval", "breakminutes", "breakeveryminutes")?.let { value ->
            value.toIntInRange(0, 240)
                ?.let { breakEveryMinutes = it }
                ?: invalidValues.add("breakEveryMinutes=$value")
        }

        ScriptArgsHelper.valueOf("breaklength", "breakseconds", "breakduration", "breaklengthseconds")?.let { value ->
            value.toIntInRange(0, 3600)
                ?.let { breakLengthSeconds = it }
                ?: invalidValues.add("breakLengthSeconds=$value")
        }

        ScriptArgsHelper.valueOf("breaklogout", "logoutbreak", "logbreak", "logoutduringbreak")?.let { value ->
            value.toBooleanArg()
                ?.let { logoutDuringBreak = it }
                ?: invalidValues.add("logoutDuringBreak=$value")
        }

        ScriptArgsHelper.valueOf("worldhop", "hop", "useworldhop")?.let { value ->
            value.toBooleanArg()
                ?.let { enabled ->
                    if (!enabled) {
                        worldHopMinutes = 0
                    } else if (worldHopMinutes <= 0) {
                        worldHopMinutes = 60
                    }
                }
                ?: invalidValues.add("worldHop=$value")
        }

        ScriptArgsHelper.valueOf("worldhopminutes", "hopminutes", "hopevery", "hopeveryminutes")?.let { value ->
            value.toIntInRange(0, 1440)
                ?.let { worldHopMinutes = it }
                ?: invalidValues.add("worldHopMinutes=$value")
        }

        return ArgsResult(invalidValues)
    }

    fun toSerializable(): UserSettings = UserSettings(
        mineLevel = mineLevel.name,
        stopAtMiningLevel = stopAtMiningLevel,
        stopAfterGemRocks = stopAfterGemRocks,
        breakEveryMinutes = breakEveryMinutes,
        breakLengthSeconds = breakLengthSeconds,
        logoutDuringBreak = logoutDuringBreak,
        worldHopMinutes = worldHopMinutes
    )

    fun fromSerializable(u: UserSettings) {
        mineLevel = MineLevel.entries.find { it.name == u.mineLevel } ?: MineLevel.LOWER
        stopAtMiningLevel = u.stopAtMiningLevel.coerceIn(0, 99)
        stopAfterGemRocks = u.stopAfterGemRocks.coerceIn(0, 9999)
        breakEveryMinutes = u.breakEveryMinutes.coerceIn(0, 240)
        breakLengthSeconds = u.breakLengthSeconds.coerceIn(0, 3600)
        logoutDuringBreak = u.logoutDuringBreak
        worldHopMinutes = u.worldHopMinutes.coerceIn(0, 1440)
    }

    data class ArgsResult(
        val invalidValues: List<String>
    ) {
        val hasWarnings: Boolean = invalidValues.isNotEmpty()
    }

    private fun ScriptArgsHelper.valueOf(vararg keys: String): String? =
        keys.firstNotNullOfOrNull { get(it) }

    private fun String.normalizedValue(): String =
        lowercase().filter { it.isLetterOrDigit() }

    private fun String.toBooleanArg(): Boolean? =
        when (normalizedValue()) {
            "true", "yes", "y", "1", "on", "enabled" -> true
            "false", "no", "n", "0", "off", "disabled" -> false
            else -> null
        }

    private fun String.toIntInRange(min: Int, max: Int): Int? =
        toIntOrNull()?.takeIf { it in min..max }

    private fun <T> Iterable<T>.matchArg(value: String): T? where T : Enum<T> =
        firstOrNull { enumValue ->
            enumValue.name.normalizedValue() == value.normalizedValue() ||
                enumValue.toString().normalizedValue() == value.normalizedValue()
        }
}
