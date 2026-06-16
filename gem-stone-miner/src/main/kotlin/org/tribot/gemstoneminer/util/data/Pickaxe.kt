package org.tribot.gemstoneminer.util.data

import net.runelite.api.gameval.ItemID

enum class Pickaxe(
    val requiredMiningLevel: Int,
    val requiredAttackLevel: Int,
    val ids: Set<Int>
) {
    CRYSTAL(
        71,
        70,
        setOf(
            ItemID.CRYSTAL_PICKAXE,
            ItemID.CRYSTAL_PICKAXE_INACTIVE,
            ItemID.GAUNTLET_PICKAXE,
            ItemID.GAUNTLET_PICKAXE_HM,
        )
    ),
    INFERNAL(
        61,
        60,
        setOf(
            ItemID.INFERNAL_PICKAXE,
            ItemID.INFERNAL_PICKAXE_EMPTY,
            ItemID.TRAILBLAZER_PICKAXE,
            ItemID.TRAILBLAZER_PICKAXE_EMPTY,
            ItemID.TRAILBLAZER_RELOADED_PICKAXE,
            ItemID.TRAILBLAZER_RELOADED_PICKAXE_EMPTY,
        )
    ),
    THIRD_AGE(61, 65, setOf(ItemID._3A_PICKAXE)),
    DRAGON(
        61,
        60,
        setOf(
            ItemID.DRAGON_PICKAXE,
            ItemID.DRAGON_PICKAXE_PRETTY,
            ItemID.ZALCANO_PICKAXE,
            ItemID.TRAILBLAZER_PICKAXE_NO_INFERNAL,
            ItemID.TRAILBLAZER_RELOADED_PICKAXE_NO_INFERNAL,
            ItemID.LEAGUE_TRAILBLAZER_PICKAXE,
        )
    ),
    RUNE(
        41,
        40,
        setOf(
            ItemID.RUNE_PICKAXE,
            ItemID.TRAIL_GILDED_PICKAXE,
            ItemID.NZONE_RUNE_PICKAXE,
        )
    ),
    ADAMANT(31, 30, setOf(ItemID.ADAMANT_PICKAXE)),
    MITHRIL(
        21,
        20,
        setOf(
            ItemID.MITHRIL_PICKAXE,
            ItemID.NZONE_MITHRIL_PICKAXE,
        )
    ),
    BLACK(11, 10, setOf(ItemID.BLACK_PICKAXE)),
    STEEL(6, 5, setOf(ItemID.STEEL_PICKAXE)),
    IRON(
        1,
        1,
        setOf(
            ItemID.IRON_PICKAXE,
            ItemID.NZONE_IRON_PICKAXE,
        )
    ),
    BRONZE(1, 1, setOf(ItemID.BRONZE_PICKAXE));

    companion object {
        val ids: Set<Int> = entries.flatMap { it.ids }.toSet()
    }
}