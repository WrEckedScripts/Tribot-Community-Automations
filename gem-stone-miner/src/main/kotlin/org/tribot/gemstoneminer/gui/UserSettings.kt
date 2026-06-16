package org.tribot.gemstoneminer.gui

import org.tribot.gemstoneminer.util.data.MineLevel

data class UserSettings(
    val mineLevel: String = MineLevel.LOWER.name,
    val stopAtMiningLevel: Int = 0,
    val stopAfterGemRocks: Int = 0,
    val breakEveryMinutes: Int = 0,
    val breakLengthSeconds: Int = 0,
    val logoutDuringBreak: Boolean = false,
    val worldHopMinutes: Int = 0
)
