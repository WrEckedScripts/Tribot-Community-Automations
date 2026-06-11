package org.tribot.wrtiaracrafter

import nullablelib.NullableLib
import nullablelib.antiban.sleepHotReaction
import nullablelib.flow.BailException
import org.tribot.automation.TribotScript
import org.tribot.automation.script.ScriptContext
import org.tribot.community.commons.randomization.Lottery
import org.tribot.wrtiaracrafter.data.Altars
import org.tribot.wrtiaracrafter.tasks.CraftTiara
import org.tribot.wrtiaracrafter.tasks.EnsureLoggedInTask
import org.tribot.wrtiaracrafter.tasks.EnterRuin
import org.tribot.wrtiaracrafter.tasks.GrabMaterials
import org.tribot.wrtiaracrafter.tasks.LeaveRuin

class WrTiaraCrafter : TribotScript {

    override fun execute(context: ScriptContext) {
        setup(context)

        val altar = Altars.AIR_ALTAR
        val stageResolver = TiaraStageResolver(context, altar)
        val ensureLoggedIn = EnsureLoggedInTask(context)
        val grabMaterials = GrabMaterials(altar)
        val enterRuin = EnterRuin(altar)
        val craftTiara = CraftTiara(altar)
        val leaveRuin = LeaveRuin(altar)
        var previousStage: TiaraStage? = null

        try {
            while (true) {
                try {
                    val stage = stageResolver.resolve()
                    if (stage != previousStage) {
                        context.logger.info("Tiara stage: $stage")
                        previousStage = stage
                    }

                    when (stage) {
                        TiaraStage.LOGIN -> ensureLoggedIn.execute()
                        TiaraStage.BANK -> grabMaterials.execute()
                        TiaraStage.TRAVEL_TO_ALTAR -> enterRuin.execute()
                        TiaraStage.CRAFT_TIARAS -> craftTiara.execute()
                        TiaraStage.EXIT_ALTAR -> leaveRuin.execute()
                        TiaraStage.OUT_OF_MATERIALS -> {
                            context.logger.info("Out of tiaras or talismans. Stopping script.")
                            return
                        }
                    }
                } catch (e: BailException) {
                    context.logger.info("Action failed: ${e.message}. Retrying next loop.")
                }

                sleepHotReaction()
            }
        } catch (e: Exception) {
            context.logger.error("Error occurred: ${e.message}", e)
            context.logger.info("Stopped WrTiaraCrafter, thanks for using it!")
            context.logger.info("If you have any feedback, please reach out on our Discord: https://discord.gg/Ju64CcbykJ")
        }
    }

    private fun setup(context: ScriptContext) {
        NullableLib.init(context)
        TiaraHud().install()
        Lottery.configure(context, true)
    }
}
