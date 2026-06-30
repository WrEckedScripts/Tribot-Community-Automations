package org.tribot.wrblastpumper.gui

import org.tribot.community.commons.ScriptArgsHelper

class PumperSettings {
    var world: Int? = null
    var refuel: Boolean = true
    var stopAt: Int = 99

    constructor()

    constructor(world: Int?, refuel: Boolean, stopAt: Int = 99) {
        this.world = world
        this.refuel = refuel
        this.stopAt = stopAt
    }

    fun copy(): PumperSettings = PumperSettings(world, refuel, stopAt)

    fun withArgumentOverrides(arguments: Map<String, String>): ArgumentResult {
        val resolved = copy()
        val warnings = mutableListOf<String>()

        arguments["world"]?.let { value ->
            value.toIntOrNull()
                ?.takeIf { it in WORLD_RANGE }
                ?.let { resolved.world = it }
                ?: warnings.add("world:$value")
        }

        arguments["refuel"]?.let { value ->
            value.toBooleanArgument()
                ?.let { resolved.refuel = it }
                ?: warnings.add("refuel:$value")
        }

        arguments["stopat"]?.let { value ->
            value.toIntOrNull()
                ?.takeIf { it in 1..99 }
                ?.let { resolved.stopAt = it }
                ?: warnings.add("stopat:$value")
        }

        return ArgumentResult(resolved, warnings)
    }

    fun installAsArguments() {
        ScriptArgsHelper.load(toArgumentString())
    }

    fun toArgumentString(): String = buildList {
        world?.let { add("world:$it") }
        add("refuel:$refuel")
        add("stopat:$stopAt")
    }.joinToString("|")

    data class ArgumentResult(
        val settings: PumperSettings,
        val invalidArguments: List<String>,
    )

    companion object {
        val WORLD_RANGE = 301..999

        private fun String.toBooleanArgument(): Boolean? = when (trim().lowercase()) {
            "true", "yes", "1", "on", "enabled" -> true
            "false", "no", "0", "off", "disabled" -> false
            else -> null
        }
    }
}
