package org.tribot.wrblastpumper

import net.runelite.api.Quest
import net.runelite.api.QuestState
import net.runelite.api.Skill
import nullablelib.NullableLib
import nullablelib.flow.BailException
import nullablelib.paint.Colors
import org.tribot.automation.TribotScript
import org.tribot.automation.script.ScriptContext
import org.tribot.script.sdk.util.TribotRandom
import org.tribot.wrblastpumper.data.PumpWorld
import org.tribot.wrblastpumper.tasks.OperatePumpTask
import org.tribot.wrblastpumper.tasks.RefreshPumpTask
import org.tribot.wrscript.utilities.hud.TaskLabelTracker
import org.tribot.wrscript.utilities.hud.WrScriptHud
import org.tribot.wrscript.utilities.tasks.EnsureLoggedInTask
import java.time.Duration
import org.tribot.script.sdk.Waiting as SdkWaiting

class WrBlastPumper : TribotScript {

    private val refreshTimer = PumpRefreshTimer()

    override fun execute(context: ScriptContext) {
        setup(context)

        val stageResolver = PumperStageResolver(context)
        val ensureLoggedIn = EnsureLoggedInTask(context)
        val operatePump = OperatePumpTask()
        val refreshPump = RefreshPumpTask(operatePump)
        var previousStage: PumperStage? = null

        context.logger.info("WrBlastPumper started")

        ensureHasRequirements()

        try {
            while (true) {
                try {
                    val stage = stageResolver.resolve(refreshTimer.isDue())
                    if (stage != previousStage) {
                        context.logger.info("Pumper stage: $stage")
                        if (stage == PumperStage.FIND_PUMP) {
                            error(
                                "Pump not found. Start the script beside the Blast Furnace pump."
                            )
                        }
                        previousStage = stage
                    }

                    when (stage) {
                        PumperStage.LOGIN -> ensureLoggedIn.execute()
                        PumperStage.UNSUPPORTED_WORLD -> {
                            val worlds = PumpWorld.numbers.sorted().joinToString()
                            error("Start WrBlastPumper on one of these worlds: $worlds")
                        }

                        PumperStage.FIND_PUMP -> {
                            TaskLabelTracker.label = "Navigating to pump"
                        }

                        PumperStage.OPERATE_PUMP -> {
                            if (operatePump.execute()) {
                                refreshTimer.scheduleNext()
                            }
                        }

                        PumperStage.REFRESH_PUMP -> {
                            if (refreshPump.execute()) {
                                refreshTimer.scheduleNext()
                            }
                        }

                        PumperStage.PUMPING -> {
                            TaskLabelTracker.label = "Pumping"
                            refreshTimer.scheduleIfMissing()
                        }
                    }
                } catch (e: BailException) {
                    context.logger.info("Action failed: ${e.message}. Retrying.")
                }

                context.waiting.sleep(TribotRandom.uniform(750, 1_500).toLong())
            }
        } catch (e: Exception) {
            context.logger.error("WrBlastPumper stopped: ${e.message}", e)
        }
    }

    private fun ensureHasRequirements() {
        if (!NullableLib.ctx.login.isLoggedIn()) {
            SdkWaiting.waitUntil(30_000) {
                NullableLib.ctx.login.login() && NullableLib.ctx.login.isLoggedIn()
            }

            val state = requireNotNull(NullableLib.ctx.clientThread.executeBlocking {
                Quest.THE_GIANT_DWARF.getState(NullableLib.ctx.clientRaw)
            })

            if (state == QuestState.NOT_STARTED) {
                throw Exception("You need to start The Giant Dwarf quest to use this script.")
            }
        }
    }

    private fun setup(context: ScriptContext) {
        NullableLib.init(context)
        WrScriptHud(
            scriptName = "WrBlastPumper",
            skill = Skill.STRENGTH,
            accent = Colors.success,
            panelFill = Colors.panelBg,
        ).row("Pump refresh") {
            formatRefreshTime(refreshTimer.remainingTime())
        }.install()
    }

    private fun formatRefreshTime(remaining: Duration?): String {
        if (remaining == null) return "Not scheduled"

        val totalSeconds = remaining.seconds
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
