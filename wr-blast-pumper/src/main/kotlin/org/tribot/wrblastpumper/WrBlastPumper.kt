package org.tribot.wrblastpumper

import net.runelite.api.*
import net.runelite.api.gameval.ItemID
import net.runelite.api.gameval.ObjectID
import nullablelib.NullableLib
import nullablelib.antiban.sleepHotReaction
import nullablelib.core.tabs.Skills
import nullablelib.flow.BailException
import nullablelib.paint.Colors
import org.tribot.automation.TribotScript
import org.tribot.automation.script.ScriptContext
import org.tribot.community.commons.ScriptArgsHelper
import org.tribot.script.sdk.util.TribotRandom
import org.tribot.wrblastpumper.data.BlastFurnaceObject
import org.tribot.wrblastpumper.data.PumpWorld
import org.tribot.wrblastpumper.gui.PumperGui
import org.tribot.wrblastpumper.gui.PumperProfileStore
import org.tribot.wrblastpumper.gui.PumperSettings
import org.tribot.wrblastpumper.tasks.HopWorld
import org.tribot.wrblastpumper.tasks.OperatePumpTask
import org.tribot.wrblastpumper.tasks.RefreshPumpTask
import org.tribot.wrblastpumper.tasks.RefuelStoveTask
import org.tribot.wrscript.utilities.hud.TaskLabelTracker
import org.tribot.wrscript.utilities.hud.WrScriptHud
import org.tribot.wrscript.utilities.tasks.EnsureLoggedInTask
import java.time.Duration
import org.tribot.script.sdk.Waiting as SdkWaiting
import org.tribot.script.sdk.util.Retry as SdkRetry

/**
 * Supported arguments:
 *  - world: int (optional, to run on a custom world)
 *  - refuel: true|false (optional, defaults to true)
 *  - profile: profile name (optional, loads the profile and bypasses the GUI)
 *  - stopat: 1..99 (stops script at provided target strength level)
 */
class WrBlastPumper : TribotScript {

    private val refreshTimer = PumpRefreshTimer()
    private val stoveRefuelTimer = StoveRefuelTimer()

    override fun execute(context: ScriptContext) {
        NullableLib.init(context)
        ScriptArgsHelper.load(context.runtime.scriptArgs)

        val profileStore = PumperProfileStore()
        val settings = resolveSettings(context, profileStore) ?: return
        settings.installAsArguments()
        setup(context)

        val stageResolver = PumperStageResolver(context)
        val ensureLoggedIn = EnsureLoggedInTask(context, true)
        val hopWorld = HopWorld(context, PumpWorld.numbers, ScriptArgsHelper.get("world")?.toIntOrNull())
        val operatePump = OperatePumpTask()
        val refreshPump = RefreshPumpTask(operatePump)
        val refuelStove = RefuelStoveTask()

        var previousStage: PumperStage? = null

        context.logger.info("WrBlastPumper started")

        ensureHasRequirements()
        ensureHasInventorySpots(context)

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
                        PumperStage.LOGIN -> {
                            ensureLoggedIn.execute()
                            refreshTimer.reset()
                        }

                        PumperStage.UNSUPPORTED_WORLD -> {
                            context.logger.error("Unsupported world detected: ${context.client.world} -> Hopping")

                            val hopped = SdkRetry.retry(3) {
                                hopWorld.execute()
                            }

                            if (!hopped) {
                                error("Failed to world-hop in 3 attempts")
                            }
                        }

                        PumperStage.FIND_PUMP -> {
                            TaskLabelTracker.label = "Searching for pump"
                            context.logger.error("Searching for pump")
                            sleepHotReaction()

                            if (!isInsideBlastFurnaceArea(context)) {
                                error("We're not inside the Blast Furnace! Be sure to start the script there.")
                            }
                        }

                        PumperStage.REFUEL_STOVE -> {
                            if (ScriptArgsHelper.getOrDefault("refuel", "true") == "false") {
                                TaskLabelTracker.label = "Skipping and waiting on stove refuel"
                                return
                            }

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

    private fun ensureHasInventorySpots(context: ScriptContext) {
        if (!NullableLib.ctx.login.isLoggedIn()) {
            SdkWaiting.waitUntil(30_000) {
                NullableLib.ctx.login.login() && NullableLib.ctx.login.isLoggedIn()
            }
        }

        val hasRegularSpade = context.inventory.contains(ItemID.SPADE)
        val hasCokeSpade = context.inventory.contains(ItemID.BLAST_FURNACE_COKE_SPADE)
        val hasAnySpade = hasRegularSpade || hasCokeSpade

        if (!context.inventory.isFull() || hasAnySpade) {
            context.logger.info("Inventory checked, we can start pumping!")
            return
        }
    }

    private fun ensureHasRequirements() {
        if (!EnsureLoggedInTask(NullableLib.ctx, true).execute()) {
            error("Failed to login")
        }

        // Validate if player has at least The Giant Dwarf quest started
        val state = requireNotNull(NullableLib.ctx.clientThread.executeBlocking {
            Quest.THE_GIANT_DWARF.getState(NullableLib.ctx.clientRaw)
        })

        if (state == QuestState.NOT_STARTED) {
            throw Exception("You need to start The Giant Dwarf quest to use this script.")
        }

        // And to facilitate stove maintenance, level 30 Firemaking is required
        if (Skills.getLevel(Skill.FIREMAKING) < 30 && ScriptArgsHelper.getOrDefault("refuel", "true") == "true") {
            throw Exception("You need to be level 30 Firemaking, to refuel the stove and thus to use this script.")
        }
    }

    private fun setup(context: ScriptContext) {
        context.logger.debug("WrBlastPumper resolved arguments: ${ScriptArgsHelper.getAll()}")

        ScriptArgsHelper.getAll().forEach { (key, value) ->
            context.logger.debug("$key=$value")
        }

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

    private fun resolveSettings(
        context: ScriptContext,
        profileStore: PumperProfileStore,
    ): PumperSettings? {
        val suppliedArguments = ScriptArgsHelper.getAll().toMap()
        val profileName = suppliedArguments["profile"]

        if (profileName != null) {
            val profile = profileStore.load(profileName)
            if (profile == null) {
                context.logger.error("WrBlastPumper profile not found: \"$profileName\"")
                return null
            }

            val resolved = profile.withArgumentOverrides(suppliedArguments)
            logInvalidArguments(context, resolved.invalidArguments)
            context.logger.info("Loaded WrBlastPumper profile \"$profileName\"; bypassing GUI")
            return resolved.settings
        }

        val initial = profileStore.load(PumperProfileStore.LAST_RUN_PROFILE)
            ?: PumperSettings()
        val resolved = initial.withArgumentOverrides(suppliedArguments)
        logInvalidArguments(context, resolved.invalidArguments)

        val settings = PumperGui(profileStore).showAndWait(resolved.settings)
        if (settings == null) {
            context.logger.info("WrBlastPumper GUI closed; stopping script")
        }
        return settings
    }

    private fun logInvalidArguments(context: ScriptContext, invalidArguments: List<String>) {
        if (invalidArguments.isNotEmpty()) {
            context.logger.warn(
                "Ignoring invalid WrBlastPumper argument(s): ${invalidArguments.joinToString(", ")}",
            )
        }
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
            val stoveIsMedium = ObjectID.BLAST_FURNACE_STOVE_MEDIUM in nearbyObjectIds
            val stoveIsFull = ObjectID.BLAST_FURNACE_STOVE_FULL in nearbyObjectIds

            stoveRefuelTimer.observe(stoveIsLow, stoveIsFull)

//            context.logger.info(
//                "Blast Furnace readings: " +
//                        "fuelLow=${client.getVarbitValue(VarbitID.BLAST_FURNACE_FUEL_LOW_READING)}, " +
//                        "stoveLow=$stoveIsLow, " +
//                        "stoveMedium=$stoveIsMedium, " +
//                        "stoveFull=$stoveIsFull"
//            )
        }
    }

    private fun isInsideBlastFurnaceArea(context: ScriptContext): Boolean =
        BlastFurnaceObject.PUMP.area.contains(context.client.localPlayer.worldLocation)

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
