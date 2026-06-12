package org.tribot.wrscript.utilities.tasks

import org.tribot.automation.script.ScriptContext
import org.tribot.script.sdk.Waiting

class EnsureLoggedInTask(private val context: ScriptContext) : TaskContract {
    override val name = "Ensure logged in"

    override fun perform(): Boolean {
        if (context.login.isLoggedIn() && context.client.localPlayer != null) return true

        return Waiting.waitUntil(15_000) {
            context.login.login() &&
                    context.login.isLoggedIn() &&
                    context.client.localPlayer != null
        }
    }
}
