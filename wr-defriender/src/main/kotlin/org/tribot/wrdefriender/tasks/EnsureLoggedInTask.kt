package org.tribot.wrdefriender.tasks

import org.tribot.automation.script.ScriptContext
import org.tribot.wrdefriender.contracts.TaskContract
import org.tribot.script.sdk.Waiting as SdkWaiting

class EnsureLoggedInTask(private val ctx: ScriptContext) : TaskContract {
    override val name = "Logging in"

    override fun perform(): Boolean {
        if (ctx.login.isLoggedIn() && ctx.client.localPlayer != null) return true

        return SdkWaiting.waitUntil(15_000) {
            ctx.login.login() &&
                    ctx.login.isLoggedIn() &&
                    ctx.client.localPlayer != null
        }
    }
}
