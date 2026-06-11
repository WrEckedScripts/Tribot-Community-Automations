package org.tribot.wrtiaracrafter.antiban

import nullablelib.NullableLib.ctx
import nullablelib.antiban.sleepIdleWakeup
import nullablelib.flow.bail
import org.tribot.community.commons.randomization.Lottery
import org.tribot.script.sdk.input.Mouse as SdkMouse
import org.tribot.script.sdk.util.TribotRandom
import org.tribot.wrtiaracrafter.hud.TaskLabelTracker

object BreaksHelper {
    fun afkBreak(
        probabilityRange: ClosedFloatingPointRange<Double>,
        sleepTime: Long? = null,
        alwaysLeaveScreen: Boolean = false,
    ) {
        var tookBreak = false
        Lottery.execute(
            TribotRandom.uniform(
                probabilityRange.start.toInt(), probabilityRange.endInclusive.toInt()
            ).toDouble()
        ) {
            TaskLabelTracker.label = "Leaving screen"

            if (alwaysLeaveScreen) {
                SdkMouse.leaveScreen()
            } else {
                Lottery.execute(0.15..0.33) {
                    SdkMouse.leaveScreen()
                }
            }

            if (sleepTime != null) {
                ctx.waiting.sleep(sleepTime)
            }

            sleepIdleWakeup()
            tookBreak = true
        }

        if (tookBreak) {
            bail("Took a break")
        }
    }
}