package org.tribot.wrblastpumper.data

enum class PumpWorld(val number: Int) {
    WORLD_302(302),
    WORLD_303(303),
    ;

    companion object {
        val numbers = entries.mapTo(mutableSetOf()) { it.number }
    }
}
