package org.tribot.gemstoneminer.util

import org.tribot.script.sdk.Login
import org.tribot.script.sdk.Skill

class MiningStats {
    @Volatile
    var gemRocksMined = 0
        private set

    val miningLevel: Int
        get() = currentMiningLevel

    @Volatile
    private var trackingStartMs = 0L

    @Volatile
    private var startMiningXp = -1

    @Volatile
    private var currentMiningXp = 0

    @Volatile
    private var startMiningLevel = -1

    @Volatile
    private var currentMiningLevel = 1

    private var lastObservedMiningXp = -1

    fun track() {
        if (!Login.isLoggedIn()) {
            return
        }

        val xp = Skill.MINING.xp
        val level = Skill.MINING.actualLevel

        if (startMiningXp < 0) {
            startMiningXp = xp
            lastObservedMiningXp = xp
            currentMiningXp = xp
            startMiningLevel = level
            currentMiningLevel = level
            trackingStartMs = System.currentTimeMillis()
            return
        }

        if (lastObservedMiningXp in 0..<xp) {
            gemRocksMined += 1
        }

        lastObservedMiningXp = xp
        currentMiningXp = xp
        currentMiningLevel = level
    }

    fun text(): String {
        val levelsGained = if (startMiningLevel < 0) 0 else currentMiningLevel - startMiningLevel
        val xpGained = if (startMiningXp < 0) 0 else currentMiningXp - startMiningXp
        val elapsed = if (trackingStartMs == 0L) 0L else System.currentTimeMillis() - trackingStartMs

        return "$currentMiningLevel | +$levelsGained | " +
            "${formatNumber(xpGained)} (${formatNumber(perHour(xpGained, elapsed))}/hr)"
    }

    private fun perHour(count: Int, elapsedMs: Long): Int {
        if (elapsedMs <= 0) return 0
        return (count * 3600000.0 / elapsedMs).toInt()
    }

    private fun formatNumber(value: Int): String =
        "%,d".format(value)
}