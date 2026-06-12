package org.tribot.wrtiaracrafter.data

import net.runelite.api.coords.WorldPoint
import net.runelite.api.gameval.ItemID

enum class Altars(
    val objectIds: List<Int>,
    val objectNames: List<String>,
    val minimumLevel: Int,
    val entryLocation: WorldPoint,
    val exitLocation: WorldPoint,
    val talismanId: Int,
    val tiaraId: Int,
    val resultTiaraId: Int
) {
    AIR_ALTAR(
        listOf(28914, 29090, 34760, 34813),
        listOf("Air altar", "Mysterious ruins", "Altar"),
        1,
        WorldPoint(2984, 3291, 0),
        WorldPoint(2843, 4833, 0),
        ItemID.AIR_TALISMAN,
        ItemID.TIARA,
        ItemID.TIARA_AIR
    )
}
