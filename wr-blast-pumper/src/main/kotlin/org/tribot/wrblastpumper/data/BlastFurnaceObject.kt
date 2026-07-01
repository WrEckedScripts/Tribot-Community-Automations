package org.tribot.wrblastpumper.data

import net.runelite.api.coords.WorldArea
import net.runelite.api.coords.WorldPoint
import net.runelite.api.gameval.AnimationID
import net.runelite.api.gameval.ObjectID

enum class BlastFurnaceObject(
    val objectId: Int,
    val objectName: String,
    val action: String,
    val playerAnimationId: Int,
    val area: WorldArea,
) {
    PUMP(
        objectId = ObjectID.BLAST_FURNACE_PUMP,
        objectName = "Pump",
        action = "Operate",
        playerAnimationId = AnimationID.BLAST_FURNACE_HUMAN_AIR_PUMP,
        area = WorldArea(WorldPoint(1935, 4956, 0), 50, 50)

    ),
}
