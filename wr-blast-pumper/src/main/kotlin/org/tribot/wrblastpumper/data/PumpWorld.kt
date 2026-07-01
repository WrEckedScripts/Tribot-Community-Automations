package org.tribot.wrblastpumper.data

import org.tribot.community.commons.ScriptArgsHelper

enum class PumpWorld(val number: Int) {
    WORLD_302(302),
    WORLD_303(303),
    ;

    companion object {
        val numbers = acceptedWorldNumbers()

        private fun acceptedWorldNumbers(): Set<Int> {
            val defaultWorldNumbers = entries.mapTo(mutableSetOf()) { it.number }
            val customWorld = ScriptArgsHelper.get("world")?.toIntOrNull()

            if (customWorld != null) {
                return setOf(customWorld)
            }

            return defaultWorldNumbers + listOfNotNull(customWorld)
        }
    }
}
