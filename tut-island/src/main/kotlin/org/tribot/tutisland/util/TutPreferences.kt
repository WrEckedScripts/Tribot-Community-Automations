package org.tribot.tutisland.util

import org.tribot.script.sdk.antiban.PlayerPreferences
import org.tribot.script.sdk.util.TribotRandom

object TutPreferences {
    private val shortDelayMean: Int = PlayerPreferences.preference(
        "scripts.TutIsland.shortDelayMean"
    ) { g -> g.uniform(150, 300) }

    private val shortDelayStdDev: Int = PlayerPreferences.preference(
        "scripts.TutIsland.shortDelayStdDev"
    ) { g -> g.uniform(10, 50) }

    fun shortDelayMs(): Int =
        TribotRandom
            .normal(shortDelayMean.toDouble(), shortDelayStdDev.toDouble())
            .toInt()

    private val mediumDelayMean: Int = PlayerPreferences.preference(
        "scripts.TutIsland.mediumDelayMean"
    ) { g -> g.uniform(1000, 1200) }

    private val mediumDelayStdDev: Int = PlayerPreferences.preference(
        "scripts.TutIsland.mediumDelayStdDev"
    ) { g -> g.uniform(200, 400) }

    fun mediumDelayMs(): Int =
        TribotRandom
            .normal(mediumDelayMean.toDouble(), mediumDelayStdDev.toDouble())
            .toInt()

    private val longDelayMean: Int = PlayerPreferences.preference(
        "scripts.TutIsland.longDelayMean"
    ) { g -> g.uniform(2100, 4500) }

    private val longDelayStdDev: Int = PlayerPreferences.preference(
        "scripts.TutIsland.longDelayStdDev"
    ) { g -> g.uniform(250, 500) }

    fun longDelayMs(): Int =
        TribotRandom
            .normal(longDelayMean.toDouble(), longDelayStdDev.toDouble())
            .toInt()

    fun getOrCreateBool(name: String): Boolean =
        PlayerPreferences.preference("scripts.TutIsland.$name") { g -> g.uniform(0, 2) == 1 }

    fun getOrCreateIndex(name: String, size: Int): Int {
        require(size > 0)
        return PlayerPreferences.preference("scripts.TutIsland.$name") { g -> g.uniform(0, size) }
            .coerceIn(0, size - 1)
    }

    fun <T> choose(name: String, vararg options: T): T {
        require(options.isNotEmpty())
        return options[getOrCreateIndex(name, options.size)]
    }

    fun orderAB(name: String): Boolean = getOrCreateBool(name)
}