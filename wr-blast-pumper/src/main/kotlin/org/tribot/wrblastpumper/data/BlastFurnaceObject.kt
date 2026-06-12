package org.tribot.wrblastpumper.data

import net.runelite.api.gameval.AnimationID
import net.runelite.api.gameval.ObjectID

enum class BlastFurnaceObject(
    val objectId: Int,
    val objectName: String,
    val action: String,
    val playerAnimationId: Int,
) {
    PUMP(
        objectId = ObjectID.BLAST_FURNACE_PUMP,
        objectName = "Pump",
        action = "Operate",
        playerAnimationId = AnimationID.BLAST_FURNACE_HUMAN_AIR_PUMP,
    ),
}
