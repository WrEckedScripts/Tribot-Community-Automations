package org.tribot.playermover.contracts

interface TaskContract {
    val name: String

    fun execute(): Boolean
}
