package org.tribot.gemstoneminer.util.data

import net.runelite.api.gameval.ItemID
import org.tribot.script.sdk.types.WorldTile

object Constants {
    val lowerMineExitLadderTile = WorldTile(2838, 9387, 0)
    val depositBoxTile = WorldTile(2842, 9383, 0)
    val shiloBank = WorldTile(2852, 2955, 0)

    val uncutGemIds = linkedSetOf(
        ItemID.UNCUT_OPAL,
        ItemID.UNCUT_JADE,
        ItemID.UNCUT_RED_TOPAZ,
        ItemID.UNCUT_SAPPHIRE,
        ItemID.UNCUT_EMERALD,
        ItemID.UNCUT_RUBY,
        ItemID.UNCUT_DIAMOND,
        ItemID.UNCUT_DRAGONSTONE,
        ItemID.UNCUT_ONYX,
        ItemID.UNCUT_ZENYTE
    )

    val gemSackIds = linkedSetOf(ItemID.GEM_SACK_OPEN, ItemID.GEM_SACK)
    val gemToteIds = linkedSetOf(ItemID.GEM_TOTE_OPEN, ItemID.GEM_TOTE)
    val gemSatchelIds = linkedSetOf(ItemID.GEM_SATCHEL_OPEN, ItemID.GEM_SATCHEL)
    val gemPouchIds = linkedSetOf(ItemID.GEM_POUCH_OPEN, ItemID.GEM_POUCH)
    val gemBagIds = linkedSetOf(ItemID.GEM_BAG_OPEN, ItemID.GEM_BAG)
    val specialGemStorageByPriority = listOf(
        gemToteIds,
        gemSatchelIds,
        gemPouchIds
    )
    val gemStorageByPriority = listOf(
        gemSackIds,
        *specialGemStorageByPriority.toTypedArray(),
        gemBagIds
    )
    val allGemContainerIds = gemStorageByPriority.flatten().toSet()

    val chargedGloryIds = linkedSetOf(
        ItemID.AMULET_OF_GLORY_INF,
        ItemID.AMULET_OF_GLORY_6,
        ItemID.AMULET_OF_GLORY_5,
        ItemID.AMULET_OF_GLORY_4,
        ItemID.AMULET_OF_GLORY_3,
        ItemID.AMULET_OF_GLORY_2,
        ItemID.AMULET_OF_GLORY_1
    )

    val closedToOpenContainerIds = linkedMapOf(
        ItemID.GEM_SACK to ItemID.GEM_SACK_OPEN,
        ItemID.GEM_TOTE to ItemID.GEM_TOTE_OPEN,
        ItemID.GEM_SATCHEL to ItemID.GEM_SATCHEL_OPEN,
        ItemID.GEM_POUCH to ItemID.GEM_POUCH_OPEN,
        ItemID.GEM_BAG to ItemID.GEM_BAG_OPEN
    )
}