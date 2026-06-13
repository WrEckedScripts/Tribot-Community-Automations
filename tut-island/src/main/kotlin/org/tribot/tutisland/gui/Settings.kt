package org.tribot.tutisland.gui

import org.tribot.community.commons.ScriptArgsHelper
import org.tribot.tutisland.util.data.IronmanMode
import org.tribot.tutisland.util.data.Location

object Settings {
    var ironmanMode: IronmanMode = IronmanMode.STANDARD
    var walkLocation: Location = Location.NONE

    fun reset() {
        ironmanMode = IronmanMode.STANDARD
        walkLocation = Location.NONE
    }

    fun loadArgs(): ArgsResult {
        reset()
        return applyArgs()
    }

    fun applyArgs(): ArgsResult {
        val invalidValues = mutableListOf<String>()

        ScriptArgsHelper.valueOf("ironmanmode", "ironman", "mode")?.let { value ->
            IronmanMode.entries.matchArg(value)
                ?.let { ironmanMode = it }
                ?: invalidValues.add("ironmanMode=$value")
        }

        ScriptArgsHelper.valueOf("walklocation", "endlocation", "location", "walkto")?.let { value ->
            Location.entries.matchArg(value)
                ?.let { walkLocation = it }
                ?: invalidValues.add("walkLocation=$value")
        }

        return ArgsResult(invalidValues = invalidValues)
    }

    fun toSerializable(): UserSettings = UserSettings(
        ironmanMode = ironmanMode.name,
        walkLocation = walkLocation.name
    )

    fun fromSerializable(u: UserSettings) {
        ironmanMode = IronmanMode.entries.find { it.name == u.ironmanMode } ?: IronmanMode.STANDARD
        walkLocation = Location.entries.find { it.name == u.walkLocation } ?: Location.NONE
    }

    data class ArgsResult(
        val invalidValues: List<String>
    ) {
        val hasWarnings: Boolean = invalidValues.isNotEmpty()
    }

    private fun String.normalizedValue(): String =
        lowercase().filter { it.isLetterOrDigit() }

    private fun ScriptArgsHelper.valueOf(vararg keys: String): String? =
        keys.firstNotNullOfOrNull { get(it) }

    private fun <T> Iterable<T>.matchArg(value: String): T? where T : Enum<T> =
        firstOrNull { enumValue ->
            enumValue.name.normalizedValue() == value.normalizedValue() ||
                enumValue.toString().normalizedValue() == value.normalizedValue()
        }
}
