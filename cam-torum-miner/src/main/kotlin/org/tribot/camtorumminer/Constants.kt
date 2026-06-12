package org.tribot.camtorumminer

import net.runelite.api.coords.WorldPoint
import net.runelite.api.gameval.ItemID

val bankTile = WorldPoint(1451, 9568, 1)
val anvilTile = WorldPoint(1448, 9582, 1)
val depositTileEast = WorldPoint(1517, 9541, 1)
val depositTileWest = WorldPoint(1500, 9542, 1)

const val hammerId = ItemID.HAMMER
const val calcifiedDepositId = ItemID.CALCIFIED_DEPOSIT
const val blessedBoneShardsId = ItemID.BLESSED_BONE_SHARD

val pickaxeIds = setOf(
    ItemID.DRAGON_PICKAXE,
    ItemID.ZALCANO_PICKAXE,
    ItemID.TRAILBLAZER_PICKAXE_NO_INFERNAL,
    ItemID.TRAILBLAZER_RELOADED_PICKAXE_NO_INFERNAL,
    ItemID.DRAGON_PICKAXE_PRETTY,
    ItemID._3A_PICKAXE, // (lmao)
    ItemID.BRONZE_PICKAXE,
    ItemID.IRON_PICKAXE,
    ItemID.STEEL_PICKAXE,
    ItemID.BLACK_PICKAXE,
    ItemID.ADAMANT_PICKAXE,
    ItemID.MITHRIL_PICKAXE,
    ItemID.RUNE_PICKAXE,
)

