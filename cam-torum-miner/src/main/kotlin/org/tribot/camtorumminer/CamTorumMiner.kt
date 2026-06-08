package org.tribot.camtorumminer

import net.runelite.api.Skill
import nullablelib.NullableLib
import nullablelib.antiban.truncatedGaussian
import nullablelib.core.Login
import nullablelib.core.Screen
import nullablelib.core.WorldViews
import nullablelib.core.event.Events
import nullablelib.core.input.Interaction
import nullablelib.core.input.Mouse
import nullablelib.core.query.TileObjects
import nullablelib.core.query.distanceFrom
import nullablelib.core.tabs.Inventory
import nullablelib.core.tabs.Skills
import nullablelib.core.widgets.Banking
import nullablelib.core.widgets.MakeScreen
import nullablelib.flow.BailException
import nullablelib.flow.awaitUntil
import nullablelib.flow.tryAction
import nullablelib.global.debug
import nullablelib.global.log
import nullablelib.global.sleep
import nullablelib.paint.Hud
import nullablelib.paint.Paint
import org.tribot.automation.TribotScript
import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.input.InteractOptions
import java.awt.Point

class CamTorumMiner : TribotScript {
    // Calcified deposit
    // Smith Anvil
    // Mine Calcified rocks (has decorative object on it if water stream)

    enum class Activity(val label: String) {
        MineCalcified("Mining"),
        BreakDeposits("Breaking deposits"),
        DepositCalcified("Banking"),
        Unknown("Idle")
    }

    var lastAnimTick = -1

    // Paint state. Volatile because the paint dispatcher reads these off the AWT thread while the
    // script loop and game-tick listener write them.
    @Volatile
    private var startTime = 0L

    @Volatile
    private var currentActivity = Activity.Unknown

    @Volatile
    private var itemsBanked = 0

    // XP / shard tracking. Baselines are captured on the first tick we observe in-game (the script
    // may be started logged out), so trackingStart anchors the per-hour rates to actual play time.
    @Volatile
    private var trackingStart = 0L

    @Volatile
    private var startMiningXp = -1

    @Volatile
    private var currentMiningXp = 0

    @Volatile
    private var shardsGained = 0

    // Only touched by the game-tick listener, so it needs no synchronization. -1 means "no prior
    // sample yet"; the first in-game tick seeds it without counting a phantom gain.
    private var lastShardCount = -1

    override fun execute(context: ScriptContext) {
        NullableLib.init(context)

        startTime = System.currentTimeMillis()
        installPaint()

        Events.onGameTick {
            trackStats()

            val animation = WorldViews.localPlayer?.animation ?: return@onGameTick
            if (animation != -1) {
                lastAnimTick = tickCount
            }
        }

        while (true) {
            try {
                currentActivity = determineActivity()
                when (currentActivity) {
                    Activity.MineCalcified -> mineCalcified()
                    Activity.DepositCalcified -> bank()
                    Activity.BreakDeposits -> breakDeposits()
                    Activity.Unknown -> {
                        debug("Unknown activity")
                        sleep(500..1500)
                    }
                }
            } catch (_: BailException) {
                // A failed action bails out of the current step; the loop re-evaluates next pass.
            }

            sleep(500..1500)
        }
    }

    /**
     * Updates XP and bone-shard tallies once per game tick. Runs only while logged in, so it is safe
     * for the script to be started on the login screen; the baselines are seeded on the first in-game
     * tick once the login handler has taken us in-game.
     */
    private fun trackStats() {
        if (!Login.isLoggedIn()) return

        val miningXp = Skills.getXp(Skill.MINING)
        if (startMiningXp < 0) {
            startMiningXp = miningXp
            trackingStart = System.currentTimeMillis()
        }
        currentMiningXp = miningXp

        // Count only increases: a drop in shards is from banking, not loss of progress.
        val shards = Inventory.getCount(blessedBoneShardsId)
        if (lastShardCount in 0 until shards) {
            shardsGained += shards - lastShardCount
        }
        lastShardCount = shards
    }

    /**
     * Registers a compact HUD in the top-left. Rows re-evaluate each paint tick, so the readouts
     * stay current without the script having to push updates. Kept narrow and dark to stay out of
     * the way of the game view.
     */
    private fun installPaint() {
        val hud = Hud("Cam Torum Miner", position = Point(8, 8), width = 215)
            .row("Runtime") { formatDuration(System.currentTimeMillis() - startTime) }
            .row("Status") { currentActivity.label }
            .row("Mining XP") { ratedValue(miningXpGained()) }
            .row("Bone shards") { ratedValue(shardsGained) }
            .row("Banked") { ratedValue(itemsBanked) }
        Paint.add(hud)
    }

    /** Mining xp earned since tracking began, or 0 before we've been in-game. */
    private fun miningXpGained(): Int = if (startMiningXp < 0) 0 else currentMiningXp - startMiningXp

    /** Renders [count] alongside its per-hour rate, e.g. "1,234 (5,678/h)". */
    private fun ratedValue(count: Int): String {
        val elapsed = if (trackingStart == 0L) 0L else System.currentTimeMillis() - trackingStart
        return "${formatNumber(count)} (${formatNumber(perHour(count, elapsed))}/h)"
    }

    fun determineActivity(): Activity {
        val player = WorldViews.localPlayer ?: return Activity.Unknown
        val atMine = player.distanceFrom(depositTileWest) < 10 || player.distanceFrom(depositTileEast) < 10

        val hasCalcifiedDeposit = Inventory.contains(calcifiedDepositId)
        val shouldBreakRocks = (!atMine && hasCalcifiedDeposit) || (Inventory.isFull() && hasCalcifiedDeposit)
        if (shouldBreakRocks) {
            return Activity.BreakDeposits
        }

        val shouldBank = !atMine && Inventory.getItems().count() > 2
        if (shouldBank) {
            return Activity.DepositCalcified
        }

        return Activity.MineCalcified
    }

    fun mineCalcified() {
        val player = WorldViews.localPlayer ?: return

        val atMine = player.distanceFrom(depositTileWest) < 10 || player.distanceFrom(depositTileEast) < 10
        if (!atMine) {
            log("Walking to deposit tile")
            walkTo(depositTileWest)
        }

        if (tickCount - lastAnimTick <= 3) {
            // Probably mining
            return
        }

        val rock = TileObjects.closestWithName("Calcified rocks") ?: return

        // interact() rotates the camera to bring the rock into view, then clicks it.
        Interaction.interact(rock, "Mine", InteractOptions(allowMovement = false))
        sleep(truncatedGaussian(mean = 600.0, stdDev = 250.0, min = 100.0, max = 1500.0).toLong())
        // Drift the cursor off the rock while we mine (nullable-lib's post-action idle move).
        Mouse.drift(Screen.getCanvasDimensions())
    }

    fun bank() {
        val player = WorldViews.localPlayer ?: return

        if (player.distanceFrom(bankTile) >= 10) {
            walkTo(bankTile)
        }

        if (!Banking.isOpen()) {
            val bank = TileObjects.closestWithAction("Bank") ?: return
            // interact() handles camera and any final approach before clicking the booth.
            Interaction.interact(bank, "Bank")
            awaitUntil(5000) { Banking.isOpen() }
            sleep(300..1500)
        }

        val itemsToDeposit = Inventory.getItems()
            .map { it.id }
            .filter { it !in pickaxeIds && it != hammerId }
            .toSet()

        // Tally what we're about to bank for the paint readout before the items leave the inventory.
        itemsBanked += itemsToDeposit.sumOf { Inventory.getCount(it) }

        for (id in itemsToDeposit) {
            Banking.depositAll(id)
            sleep(1..25)
        }

        Banking.close()
    }

    fun breakDeposits() {
        val player = WorldViews.localPlayer ?: return

        if (player.distanceFrom(anvilTile) >= 10) {
            walkTo(fuzzTile(anvilTile))
        }

        val current = WorldViews.localPlayer ?: return
        if (current.animation == -1) {
            if (!Inventory.contains(calcifiedDepositId)) {
                return
            }
            val anvil = TileObjects.closestWithName("Anvil", maxDistance = 10) ?: return
            // "Use" the deposit, then click the anvil to use the deposit on it.
            Inventory.clickItem(calcifiedDepositId, "Use")
            Interaction.click(anvil, "Use")
            awaitUntil(2000) { MakeScreen.isVisible() }
            sleep(10..400)
        }

        if (!MakeScreen.isVisible()) {
            return
        }

        MakeScreen.setQuantity(Int.MAX_VALUE)
        MakeScreen.make(calcifiedDepositId)

        // Give the smithing animation time to register, then hold until it finishes or we
        // run out of deposits. The wait is best-effort, so swallow a bail on timeout.
        tryAction { awaitUntil(4000) { (WorldViews.localPlayer?.animation ?: -1) == -1 } }
        while (tickCount - lastAnimTick <= 4 && Inventory.contains(calcifiedDepositId)) {
            sleep(100)
        }
    }
}
