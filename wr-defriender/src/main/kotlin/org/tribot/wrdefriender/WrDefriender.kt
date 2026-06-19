package org.tribot.wrdefriender

import nullablelib.NullableLib
import nullablelib.antiban.sleepHotReaction
import org.tribot.automation.TribotScript
import org.tribot.automation.script.ScriptContext
import org.tribot.wrdefriender.hud.DefrienderHud
import org.tribot.wrdefriender.tasks.EnsureLoggedInTask
import org.tribot.wrdefriender.tasks.LogoutTask
import org.tribot.wrdefriender.tasks.RemoveFriendTask

class WrDefriender : TribotScript {
    override fun execute(context: ScriptContext) {
        NullableLib.init(context)
        DefrienderState.task = "Starting"
        DefrienderState.removed = 0
        DefrienderHud().install()
        context.logger.info("WrDefriender started!")

        val loginTask = EnsureLoggedInTask(context)
        val removeFriendTask = RemoveFriendTask(context)
        val logoutTask = LogoutTask(context)
        var failures = 0

        try {
            while (true) {
                val succeeded = when {
                    !context.login.isLoggedIn() || context.client.localPlayer == null ->
                        loginTask.execute()

                    context.client.friendContainer.count < 0 -> {
                        DefrienderState.task = "Loading friends"
                        true
                    }

                    context.client.friendContainer.count == 0 -> break
                    else -> removeFriendTask.execute()
                }

                failures = if (succeeded) 0 else failures + 1
                check(failures < 5) { "Task failed five times in a row" }
                sleepHotReaction()
            }

            check(logoutTask.execute()) { "Failed to log out" }
            DefrienderState.task = "Finished"
            context.logger.info("Removed ${DefrienderState.removed} friends. WrDefriender finished!")
            context.logger.info("If you have any feedback, please reach out on our Discord: https://discord.gg/Ju64CcbykJ")
        } catch (e: Exception) {
            context.logger.error("WrDefriender stopped: ${e.message}", e)
        }
    }
}
