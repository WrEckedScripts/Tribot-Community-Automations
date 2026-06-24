package org.tribot.gemstoneminer.util.taskmanagement

class TaskScheduler(
    private val tasks: List<Task>
) {
    var currentTask: Task? = null
        private set

    fun tick() {
        val next = tasks
            .asSequence()
            .filter { it.canRun() }
            .maxByOrNull { it.priority }

        currentTask = next
        next?.execute()
    }
}