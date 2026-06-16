package org.tribot.gemstoneminer.gui

internal enum class StopConditionType(
    val displayName: String
) {
    NONE("None"),
    MINING_LEVEL("Mining Level"),
    GEM_ROCKS("Gem Rocks")
}