package org.tribot.gemstoneminer.util

import org.tribot.gemstoneminer.gui.Settings
import org.tribot.gemstoneminer.util.data.MineLevel
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Log

object MineLevelAccess {
    fun enforceSelectedMineLevel() {
        if (Settings.mineLevel != MineLevel.LOWER) {
            return
        }

        val diaryVarbit = GameState.getVarbit(3598)
        if (diaryVarbit < 1) {
            Log.warn(
                "[GemStoneMiner] Lower mine selected, but Karamja Medium Diary is not complete " +
                    "(varbit 3598=$diaryVarbit). Using upper mine instead."
            )
            Settings.mineLevel = MineLevel.UPPER
        }
    }
}