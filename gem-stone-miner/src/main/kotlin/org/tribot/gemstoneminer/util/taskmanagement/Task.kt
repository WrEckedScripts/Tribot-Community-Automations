package org.tribot.gemstoneminer.util.taskmanagement

interface Task {
    val displayName: String
    val priority: Int
    fun canRun(): Boolean
    fun execute()
}