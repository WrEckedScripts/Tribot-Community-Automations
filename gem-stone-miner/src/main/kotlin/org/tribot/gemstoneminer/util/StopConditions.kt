package org.tribot.gemstoneminer.util

import org.tribot.gemstoneminer.gui.Settings

class StopConditions(
    private val miningStats: MiningStats
) {
    fun shouldStop(): Boolean =
        stopReason() != null

    fun stopReason(): String? {
        if (Settings.stopAtMiningLevel > 0 && miningStats.miningLevel >= Settings.stopAtMiningLevel) {
            return "Reached Mining level ${miningStats.miningLevel}."
        }

        if (Settings.stopAfterGemRocks > 0 && miningStats.gemRocksMined >= Settings.stopAfterGemRocks) {
            return "Mined ${miningStats.gemRocksMined} gem rocks."
        }

        return null
    }
}