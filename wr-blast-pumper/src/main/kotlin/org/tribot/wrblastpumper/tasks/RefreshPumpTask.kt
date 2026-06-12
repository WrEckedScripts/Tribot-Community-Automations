package org.tribot.wrblastpumper.tasks

import org.tribot.wrscript.utilities.tasks.TaskContract

class RefreshPumpTask(
    private val operatePumpTask: OperatePumpTask,
) : TaskContract {
    override val name = "Refreshing pump"

    override fun perform(): Boolean = operatePumpTask.perform()
}
