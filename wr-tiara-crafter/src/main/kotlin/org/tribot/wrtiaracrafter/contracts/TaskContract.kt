package org.tribot.wrtiaracrafter.contracts

interface TaskContract {
    val name: String

    fun execute(): Boolean
}
