package org.tribot.wrdefriender.contracts

import org.tribot.wrdefriender.DefrienderState

interface TaskContract {
    val name: String

    fun execute(): Boolean {
        DefrienderState.task = name
        return perform()
    }

    fun perform(): Boolean
}
