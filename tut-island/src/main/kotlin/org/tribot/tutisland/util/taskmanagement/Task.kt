package org.tribot.tutisland.util.taskmanagement

interface Task {
    val displayName: String
    val priority: Int
    fun canRun(): Boolean
    fun execute()
}