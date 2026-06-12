package org.tribot.wrtiaracrafter.antiban

import nullablelib.NullableLib.ctx
import nullablelib.antiban.sleepIdleWakeup
import nullablelib.flow.bail
import org.tribot.community.commons.randomization.Lottery
import org.tribot.script.sdk.util.TribotRandom
import org.tribot.script.sdk.input.Mouse as SdkMouse
import org.tribot.wrtiaracrafter.hud.TaskLabelTracker

object BreaksHelper {
    fun afkBreak(
        probabilityRange: ClosedFloatingPointRange<Double>,
        sleepTime: Long? = null,
        alwaysLeaveScreen: Boolean = false,
    ) {
        var tookBreak = false
        Lottery.execute(probabilityRange) {
            TaskLabelTracker.label = "Leaving screen"

            if (alwaysLeaveScreen) {
                SdkMouse.leaveScreen()
            } else {
                val lowEnd = TribotRandom.uniform(0.11, 0.15)
                val highEnd = TribotRandom.uniform(0.33, 0.37)

                Lottery.execute(lowEnd..highEnd) {
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