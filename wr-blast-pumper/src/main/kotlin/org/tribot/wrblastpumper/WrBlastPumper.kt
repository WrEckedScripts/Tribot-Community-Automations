package org.tribot.wrblastpumper

import net.runelite.api.*
import net.runelite.api.gameval.ObjectID
import net.runelite.api.gameval.VarbitID
import nullablelib.NullableLib
import nullablelib.core.tabs.Skills
import nullablelib.flow.BailException
import nullablelib.paint.Colors
import org.tribot.automation.TribotScript
import org.tribot.automation.script.ScriptContext
import org.tribot.script.sdk.util.TribotRandom
import org.tribot.wrblastpumper.tasks.OperatePumpTask
import org.tribot.wrblastpumper.tasks.RefreshPumpTask
import org.tribot.wrblastpumper.tasks.RefuelStoveTask
import org.tribot.wrscript.utilities.hud.TaskLabelTracker
import org.tribot.wrscript.utilities.hud.WrScriptHud
import org.tribot.wrscript.utilities.tasks.EnsureLoggedInTask
import java.time.Duration
import org.tribot.script.sdk.Waiting as SdkWaiting

class WrBlastPumper : TribotScript {

    private val refreshTimer = PumpRefreshTimer()
    private val stoveRefuelTimer = StoveRefuelTimer()

    override fun execute(context: ScriptContext) {
        setup(context)

        val stageResolver = PumperStageResolver(context)
        val ensureLoggedIn = EnsureLoggedInTask(context)
        val operatePump = OperatePumpTask()
        val refreshPump = RefreshPumpTask(operatePump)
        val refuelStove = RefuelStoveTask()
        var previousStage: PumperStage? = null

        context.logger.info("WrBlastPumper started")

        ensureHasRequirements()

        try {
            while (true) {
                try {
                    val stage = stageResolver.resolve(
                        isRefreshDue = refreshTimer.isDue(),
                        isRefuelDue = stoveRefuelTimer.isDue(),
                    )

                    if (stage != previousStage) {
                        context.logger.info("Pumper stage: $stage")
                        previousStage = stage
                    }

                    when (stage) {
                        PumperStage.LOGIN -> ensureLoggedIn.execute()
//                        PumperStage.UNSUPPORTED_WORLD -> {
//                            val worlds = PumpWorld.numbers.sorted().joinToString()
//                            context.logger.error("Unsupported world: ${context.client.world}")
//                            error("Start WrBlastPumper on one of these worlds: $worlds")
//                        }

                        PumperStage.FIND_PUMP -> {
                            TaskLabelTracker.label = "Searching for pump"
                            error("Pump not found. Start the script beside the Blast Furnace pump.")
                        }

                        PumperStage.REFUEL_STOVE -> {
                            if (refuelStove.execute()) {
                                stoveRefuelTimer.clear()
                            }
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

                        else -> error("Unknown stage: $stage")
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

            // Validate if player has at least The Giant Dwarf quest started
            val state = requireNotNull(NullableLib.ctx.clientThread.executeBlocking {
                Quest.THE_GIANT_DWARF.getState(NullableLib.ctx.clientRaw)
            })

            if (state == QuestState.NOT_STARTED) {
                throw Exception("You need to start The Giant Dwarf quest to use this script.")
            }

            // And to facilitate stove maintenance, level 30 Firemaking is required
            if (Skills.getLevel(Skill.FIREMAKING) < 30) {
                throw Exception("You need to be level 30 Firemaking, to refuel the stove and thus to use this script.")
            }
        }
    }

    private fun setup(context: ScriptContext) {
        NullableLib.init(context)
        installBlastFurnaceDebugListener(context)

        WrScriptHud(
            scriptName = "WrBlastPumper",
            skill = Skill.STRENGTH,
            accent = Colors.success,
            panelFill = Colors.panelBg,
        ).row("Pump re-interact") {
            formatRefreshTime(refreshTimer.remainingTime())
        }.row("Stove") {
            stoveRefuelTimer.getDueLabel()
        }.install()
    }

    private fun installBlastFurnaceDebugListener(context: ScriptContext) {
        context.events.onGameTick {
            if (!context.login.isLoggedIn()) return@onGameTick

            val client = context.clientRaw
            val nearbyObjectIds = context.worldViews.withTiles(maxDistance = 8) { tiles ->
                tiles.flatMap(::tileObjects)
                    .map { it.id }
                    .filter { it in STOVE_OBJECT_IDS }
                    .toSet()
            }.orEmpty()
            val stoveIsLow = ObjectID.BLAST_FURNACE_STOVE_LOW in nearbyObjectIds
            val stoveIsFull = ObjectID.BLAST_FURNACE_STOVE_FULL in nearbyObjectIds

            stoveRefuelTimer.observe(stoveIsLow, stoveIsFull)

            context.logger.info(
                "Blast Furnace readings: " +
                        "fuelLow=${client.getVarbitValue(VarbitID.BLAST_FURNACE_FUEL_LOW_READING)}, " +
                        "stoveLow=$stoveIsLow, " +
                        "stoveMedium=${ObjectID.BLAST_FURNACE_STOVE_MEDIUM in nearbyObjectIds}, " +
                        "stoveFull=$stoveIsFull"
            )
        }
    }

    private fun tileObjects(tile: Tile): Sequence<TileObject> = sequence {
        tile.wallObject?.let { yield(it) }
        tile.decorativeObject?.let { yield(it) }
        tile.groundObject?.let { yield(it) }
        tile.gameObjects.filterNotNull().forEach { yield(it) }
    }

    private fun formatRefreshTime(remaining: Duration?): String {
        if (remaining == null) return "Not scheduled"

        val totalSeconds = remaining.seconds
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    private companion object {
        val STOVE_OBJECT_IDS = setOf(
            ObjectID.BLAST_FURNACE_STOVE_LOW,
            ObjectID.BLAST_FURNACE_STOVE_MEDIUM,
            ObjectID.BLAST_FURNACE_STOVE_FULL,
        )
    }
}
