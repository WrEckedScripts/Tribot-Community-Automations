package org.tribot.tutisland.util

import kotlin.random.Random

object OsrsNameGenerator {
    private const val MIN_LEN = 5
    private const val MAX_LEN = 12

    private val firstWords = listOf(
        "Ash", "Oak", "Yew", "Rune", "Iron", "Lum", "Var", "Fal", "Dray", "Moss",
        "Tiny", "Swift", "Lucky", "Quiet", "Brave", "Red", "Blue", "Grey",
        "Aero", "Aqua", "Ardy", "Barb", "Birch", "Black", "Bright", "Bronze",
        "Cairn", "Cedar", "Chill", "Cloud", "Copper", "Crag", "Dark", "Dawn",
        "Dusk", "Dust", "East", "Edge", "Elm", "Ember", "Fenn", "Frost",
        "Gold", "Green", "Haze", "Hill", "Jade", "Karam", "Lunar", "Maple",
        "Mist", "Moon", "North", "Nova", "Opal", "Pearl", "Pine", "Quick",
        "River", "Sage", "Sand", "Shadow", "Silver", "Sky", "Snow", "South",
        "Steel", "Stone", "Storm", "Sun", "Tarn", "Vale", "West", "Wild",
        "Willow", "Wind", "Winter"
    )

    private val secondWords = listOf(
        "Lad", "Mage", "Miner", "Chef", "Fish", "Smith", "Bow", "Log", "Imp",
        "Rat", "Quest", "Scout", "Wiz", "Fox", "Star", "Gem",
        "Ace", "Axe", "Baker", "Bard", "Blade", "Bolt", "Cook", "Crafter",
        "Dart", "Dust", "Fang", "Guard", "Herb", "Hunter", "Knight", "Ore",
        "Path", "Pick", "Rune", "Sage", "Seer", "Shard", "Spark", "Stone",
        "Tide", "Trail", "Ward", "Weaver"
    )

    private val syllables = listOf(
        "an", "ar", "bel", "cor", "dan", "dor", "el", "en", "fin", "gan",
        "hal", "ian", "jar", "kal", "lan", "lor", "mar", "mir", "mor", "nar",
        "or", "ran", "ren", "ric", "rin", "ros", "sar", "sel", "tan", "tor",
        "val", "ven", "wyn", "zan"
    )

    fun generate(rng: Random = Random.Default): String {
        // Try a few simple name styles until one fits OSRS name rules. If the game
        // says the name is taken, the display-name task will generate another one.
        repeat(50) {
            val candidate = when (rng.nextInt(8)) {
                0 -> "${firstWords.random(rng)}${secondWords.random(rng)}"
                1 -> "${firstWords.random(rng)}${number(rng)}"
                2 -> "${secondWords.random(rng)}${number(rng)}"
                3 -> "${firstWords.random(rng)}${secondWords.random(rng)}${rng.nextInt(2, 10)}"
                4 -> "${syllables.random(rng).capitalized()}${syllables.random(rng)}${number(rng)}"
                5 -> "${firstWords.random(rng)}${syllables.random(rng)}"
                6 -> "${syllables.random(rng).capitalized()}${secondWords.random(rng)}"
                else -> "${secondWords.random(rng)}${syllables.random(rng)}${rng.nextInt(2, 100)}"
            }.cleanName()

            if (candidate.length in MIN_LEN..MAX_LEN) {
                return candidate
            }
        }

        return "New${secondWords.random(rng)}${number(rng)}".cleanName().take(MAX_LEN)
    }

    private fun number(rng: Random): Int =
        when (rng.nextInt(3)) {
            0 -> rng.nextInt(2, 10)
            1 -> rng.nextInt(10, 100)
            else -> rng.nextInt(100, 999)
        }

    private fun String.cleanName(): String =
        replace(Regex("[^A-Za-z0-9]"), "")

    private fun String.capitalized(): String =
        replaceFirstChar { it.uppercaseChar() }
}
