package org.tribot.gemstoneminer

import org.tribot.community.commons.ScriptArgsHelper
import org.tribot.gemstoneminer.gui.GUI
import org.tribot.gemstoneminer.gui.ProfileStore
import org.tribot.gemstoneminer.gui.Settings
import org.tribot.gemstoneminer.gui.paint.GemStoneMinerPaint
import org.tribot.gemstoneminer.tasks.*
import org.tribot.gemstoneminer.util.*
import org.tribot.gemstoneminer.util.taskmanagement.Task
import org.tribot.gemstoneminer.util.taskmanagement.TaskScheduler
import org.tribot.script.sdk.Camera
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.Login
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.script.ScriptConfig
import org.tribot.script.sdk.script.TribotScript
import org.tribot.script.sdk.util.TribotRandom

class GemStoneMiner: TribotScript {
    private val tasks: List<Task> = listOf(
        DepositLowerMine(),
        BankTask(),
        OpenGemStorage(),
        UsePickaxeSpecial(),
        MineGemRocks(),

        //TODO Remove when Dentist Walker handles the ladder path link.

//      Keeping the tasks here as reference if needed for other links but ideally we add path link to walker
        EnterLowerMine(),
        LeaveLowerMine(),
    )

    override fun configure(config: ScriptConfig) {
        config.isRandomsAndLoginHandlerEnabled = false
    }

    override fun execute(args: String) {
        ScriptArgsHelper.load(args)
        val store = ProfileStore()
        if (args.isBlank()) {
            val (_, didStart) = GUI.showComposeGuiAndWait(store)
            if (!didStart) {
                Log.info("[GemStoneMiner] GUI closed, stopping script.")
                return
            }
        } else if (!loadArgs(args, store)) {
            return
        }

        while (!Login.isLoggedIn()) {
            Login.login()
            Log.info("[GemStoneMiner] Logging in...")
            Waiting.wait(1000)
        }

        MineLevelAccess.enforceSelectedMineLevel()
        logStartSettings()

        val startTimeMs = System.currentTimeMillis()

        val microBreaks = MicroBreaks()
        val worldHopManager = WorldHopManager()
        val scheduler = TaskScheduler(tasks)
        val miningStats = MiningStats()
        miningStats.track()
        val stopConditions = StopConditions(miningStats)

        GemStoneMinerPaint(
            statusProvider = {
                if (microBreaks.isBreaking) {
                    microBreaks.statusText
                } else {
                    scheduler.currentTask?.displayName ?: "Idle"
                }
            },
            rocksMinedProvider = { miningStats.gemRocksMined },
            miningStatsProvider = { miningStats.text() },
            nextBreakProvider = { microBreaks.timeUntilNextBreakText() },
            nextWorldHopProvider = { worldHopManager.timeUntilNextHopText() },
            startTime = startTimeMs
        ).install()

        Camera.setRotationMethod(Camera.RotationMethod.MOUSE)

        while (!stopConditions.shouldStop()) {

            miningStats.track()

            if (stopConditions.shouldStop()) {
                break
            }

            if (microBreaks.tick()) {
                continue
            }

            if (!ensureLoggedIn()) {
                continue
            }

            if (worldHopManager.tick()) {
                continue
            }

            scheduler.tick()
            Waiting.wait(TribotRandom.uniform(50, 200))
        }

        stopConditions.stopReason()?.let { reason ->
            Log.info("[GemStoneMiner] $reason Stopping script.")
        }
    }

    private fun ensureLoggedIn(): Boolean {
        if (Login.isLoggedIn()) {
            return true
        }

        Login.login()
        Waiting.waitUntil(GemStoneMinerPreferences.longDelayMs() * 2) { Login.isLoggedIn() }
        Waiting.wait(GemStoneMinerPreferences.mediumDelayMs())
        return Login.isLoggedIn()
    }

    private fun loadArgs(args: String, store: ProfileStore): Boolean {
        ScriptArgsHelper.get("raw")?.let { return loadProfile(it, store) }

        val profile = ScriptArgsHelper.get("profile")
            ?: ScriptArgsHelper.get("profilename")

        if (profile != null && !loadProfile(profile, store)) {
            return false
        }

        val result = if (profile == null) {
            Settings.loadArgs()
        } else {
            Settings.applyArgs()
        }

        if (result.invalidValues.isNotEmpty()) {
            Log.warn("[GemStoneMiner] Invalid arg value(s): ${result.invalidValues.joinToString(", ")}")
        }

        Log.info(
            "[GemStoneMiner] Loaded inline args | level=${Settings.mineLevel.displayName} " +
                "stopLevel=${Settings.stopAtMiningLevel} " +
                "stopRocks=${Settings.stopAfterGemRocks} " +
                "microBreakEvery=${Settings.breakEveryMinutes}m " +
                "microBreakLength=${Settings.breakLengthSeconds}s " +
                "worldHopEvery=${Settings.worldHopMinutes}m"
        )
        return true
    }

    private fun loadProfile(profileArg: String, store: ProfileStore): Boolean {
        val profile = profileArg.trim()
            .removeSuffix(".properties")
            .removeSuffix(".json")

        val loaded = store.load(profile)
        if (loaded == null) {
            Log.error("[GemStoneMiner] Profile not found: \"$profile\"")
            return false
        }

        Settings.fromSerializable(loaded)
        Log.info("[GemStoneMiner] Loaded profile \"$profile\"")
        return true
    }

    private fun logStartSettings() {
        Log.info(
            "[GemStoneMiner] Starting | level=${Settings.mineLevel.displayName} " +
                "stopLevel=${Settings.stopAtMiningLevel} " +
                "stopRocks=${Settings.stopAfterGemRocks} " +
                "microBreakEvery=${Settings.breakEveryMinutes}m " +
                "microBreakLength=${Settings.breakLengthSeconds}s " +
                "worldHopEvery=${Settings.worldHopMinutes}m"
        )
    }
}