package org.tribot.wrblastpumper.tasks

import net.runelite.api.Skill
import nullablelib.NullableLib
import nullablelib.antiban.sleepIdleWakeup
import org.tribot.community.commons.ScriptArgsHelper
import org.tribot.wrscript.utilities.tasks.TaskContract

class RefreshPumpTask(
    private val operatePumpTask: OperatePumpTask,
) : TaskContract {
    override val name = "Refreshing pump"

    override fun perform(): Boolean {
        sleepIdleWakeup()

        // Check if we've reached our stopAt level and either short-circuit the script or continue.
        val stopAt = ScriptArgsHelper.getOrDefault("stopat", "99").toInt()
        if (NullableLib.ctx.skills.getLevel(Skill.STRENGTH) >= stopAt) {
            throw InterruptedException("Reached stopping level $stopAt, congrats on achieving your goal!")
        }

        return operatePumpTask.perform()
    }
}
