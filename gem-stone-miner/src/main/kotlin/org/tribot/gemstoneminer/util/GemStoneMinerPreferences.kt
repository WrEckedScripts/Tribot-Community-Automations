package org.tribot.gemstoneminer.util

import org.tribot.script.sdk.antiban.PlayerPreferences
import org.tribot.script.sdk.util.TribotRandom

object GemStoneMinerPreferences {
    private val shortDelayMean: Int = PlayerPreferences.preference(
        "scripts.GemStoneMiner.shortDelayMean"
    ) { g -> g.uniform(150, 300) }

    private val shortDelayStdDev: Int = PlayerPreferences.preference(
        "scripts.GemStoneMiner.shortDelayStdDev"
    ) { g -> g.uniform(10, 50) }

    fun shortDelayMs(): Int =
        TribotRandom
            .normal(shortDelayMean.toDouble(), shortDelayStdDev.toDouble())
            .toInt()
            .coerceAtLeast(50)

    private val mediumDelayMean: Int = PlayerPreferences.preference(
        "scripts.GemStoneMiner.mediumDelayMean"
    ) { g -> g.uniform(1000, 1200) }

    private val mediumDelayStdDev: Int = PlayerPreferences.preference(
        "scripts.GemStoneMiner.mediumDelayStdDev"
    ) { g -> g.uniform(200, 400) }

    fun mediumDelayMs(): Int =
        TribotRandom
            .normal(mediumDelayMean.toDouble(), mediumDelayStdDev.toDouble())
            .toInt()
            .coerceAtLeast(300)

    private val longDelayMean: Int = PlayerPreferences.preference(
        "scripts.GemStoneMiner.longDelayMean"
    ) { g -> g.uniform(2100, 4500) }

    private val longDelayStdDev: Int = PlayerPreferences.preference(
        "scripts.GemStoneMiner.longDelayStdDev"
    ) { g -> g.uniform(250, 500) }

    fun longDelayMs(): Int =
        TribotRandom
            .normal(longDelayMean.toDouble(), longDelayStdDev.toDouble())
            .toInt()
            .coerceAtLeast(1000)

    private val microBreakIntervalVariancePercent: Int = PlayerPreferences.preference(
        "scripts.GemStoneMiner.microBreakIntervalVariancePercent"
    ) { g -> g.uniform(10, 21) }

    private val microBreakLengthVariancePercent: Int = PlayerPreferences.preference(
        "scripts.GemStoneMiner.microBreakLengthVariancePercent"
    ) { g -> g.uniform(5, 16) }

    private val worldHopIntervalVariancePercent: Int = PlayerPreferences.preference(
        "scripts.GemStoneMiner.worldHopIntervalVariancePercent"
    ) { g -> g.uniform(8, 18) }

    fun randomizeMicroBreakInterval(baseMs: Long): Long =
        randomize(baseMs, microBreakIntervalVariancePercent, 1000)

    fun randomizeMicroBreakLength(baseMs: Long): Long =
        randomize(baseMs, microBreakLengthVariancePercent, 1000)

    fun randomizeWorldHopInterval(baseMs: Long): Long =
        randomize(baseMs, worldHopIntervalVariancePercent, 5000)

    private fun randomize(baseMs: Long, variancePercent: Int, minimumVarianceMs: Long): Long {
        val varianceMs = ((baseMs * variancePercent) / 100)
            .coerceAtLeast(minimumVarianceMs)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()

        return (baseMs + TribotRandom.uniform(-varianceMs, varianceMs))
            .coerceAtLeast(1000)
    }
}