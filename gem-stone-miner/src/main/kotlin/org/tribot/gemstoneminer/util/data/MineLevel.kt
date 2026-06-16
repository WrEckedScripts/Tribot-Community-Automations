package org.tribot.gemstoneminer.util.data

import org.tribot.script.sdk.types.WorldTile

enum class MineLevel(
    val displayName: String,
    val regionId: Int,
    val walkingTile: WorldTile
) {
    LOWER("Lower level", 11410, WorldTile(2841, 9384, 0)),
    UPPER("Upper level", 11310, WorldTile(2823, 3000, 0));

    override fun toString(): String = displayName
}