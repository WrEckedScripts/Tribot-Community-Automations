package org.tribot.tutisland.tasks.magicinstructor

import org.tribot.script.sdk.ChatScreen
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Magic
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.taskmanagement.Task

class CastHomeTeleport: Task {
    override val displayName = "Casting Home Teleport"
    override val priority = 20

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 680 &&
                (!ChatScreen.isOpen() || ChatScreen.isClickContinueOpen())
    }

    override fun execute() {
        if (Magic.selectSpell("Home Teleport")) {
            Waiting.waitUntil(15000) {
                !Query.npcs().nameEquals("Magic Instructor").isReachable.isAny
            }
        }
    }
}