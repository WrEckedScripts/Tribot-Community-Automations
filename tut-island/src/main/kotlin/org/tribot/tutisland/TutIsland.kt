package org.tribot.tutisland

import org.tribot.community.commons.ScriptArgsHelper
import org.tribot.script.sdk.*
import org.tribot.script.sdk.script.ScriptConfig
import org.tribot.script.sdk.script.TribotScript
import org.tribot.script.sdk.types.WorldTile
import org.tribot.script.sdk.util.TribotRandom
import org.tribot.tutisland.gui.GUI
import org.tribot.tutisland.gui.ProfileStore
import org.tribot.tutisland.gui.Settings
import org.tribot.tutisland.paint.TutIslandPaint
import org.tribot.tutisland.tasks.HandleContinueDialogue
import org.tribot.tutisland.tasks.WalkToEndingLocation
import org.tribot.tutisland.tasks.accountguide.*
import org.tribot.tutisland.tasks.bank.*
import org.tribot.tutisland.tasks.brotherbrace.*
import org.tribot.tutisland.tasks.combatinstructor.*
import org.tribot.tutisland.tasks.gielinorguide.*
import org.tribot.tutisland.tasks.magicinstructor.*
import org.tribot.tutisland.tasks.masterchef.*
import org.tribot.tutisland.tasks.mininginstructor.*
import org.tribot.tutisland.tasks.questguide.*
import org.tribot.tutisland.tasks.survivalexpert.*
import org.tribot.tutisland.util.MicroBreaks
import org.tribot.tutisland.util.taskmanagement.Task
import org.tribot.tutisland.util.taskmanagement.TaskScheduler

class TutIsland: TribotScript {
    private val noMovementTimeoutMs = 5 * 60 * 1000L
    private var lastMoveMs = 0L
    private var lastPlayerTile: WorldTile? = null

    private val tasks: List<Task> = listOf(
        HandleContinueDialogue(),

        // Character Creator
        HandleDisplayName(),
        HandleRandomCharacterCreator(),
        HandleExperienceWidget(),

        // Account Guide
        TalkToGuide(),
        OpenSettings(),
        LeaveGuide(),

        // Survival Guide
        TalkToSurvivalExpert(),
        OpenInventory(),
        FishShrimp(),
        OpenSkills(),
        CutTree(),
        LightFire(),
        CookShrimp(),
        LeaveSurvivalExpert(),

        // Master Chef
        EnterCook(),
        TalkToChef(),
        MakeDough(),
        BakeBread(),
        LeaveCook(),

        // Quest Guide
        EnterQuestGuide(),
        TalkToQuestGuide(),
        OpenQuests(),
        LeaveQuestGuide(),

        // Mining Guide
        TalkToMiningInstructor(),
        MineTin(),
        MineCopper(),
        SmeltBronzeBar(),
        MakeBronzeDagger(),
        LeaveMiningInstructor(),

        // Combat
        TalkToCombatInstructor(),
        OpenEquipment(),
        ViewEquipmentStats(),
        EquipDagger(),
        EquipSwordAndShield(),
        OpenCombat(),
        MeleeGiantRat(),
        EquipBowAndArrows(),
        RangeGiantRat(),
        LeaveCombatInstructor(),

        // Bank & Poll
        OpenBankBooth(),
        OpenPollBooth(),
        LeaveBank(),

        // Account Guide
        TalkToAccountGuide(),
        OpenAccountManagement(),
        LeaveAccountGuide(),

        // Prayer Guide
        TalkToBrotherBrace(),
        OpenPrayer(),
        LeaveBrotherBrace(),

        // Ironman Mode disabled until bank PIN handling is reliable.
        // TalkToIronmanTutor(),
        // SelectIronmanMode(),

        // Magic Guide
        TalkToMagicInstructor(),
        OpenMagic(),
        CastSpellOnChicken(),
        CastHomeTeleport(),

        WalkToEndingLocation()
    )

    override fun configure(config: ScriptConfig) {
        config.isRandomsAndLoginHandlerEnabled = true
    }

    override fun execute(args: String) {
        ScriptArgsHelper.load(args)
        val store = ProfileStore()
        if (args.isBlank()) {
            val (_, didStart) = GUI.showComposeGuiAndWait(store)
            if (!didStart) {
                Log.info("[TutIsland] GUI closed, stopping script.")
                return
            }
        } else {
            if (!loadArgs(args, store)) {
                return
            }
        }

        while (!Login.isLoggedIn()) {
            Login.login()
            Log.info("[TutIsland] Logging in...")
            Waiting.wait(1000)
        }

        val startTimeMs = System.currentTimeMillis()
        lastMoveMs = startTimeMs
        lastPlayerTile = MyPlayer.getTile()

        val scheduler = TaskScheduler(tasks) {
            Log.info("[TutIsland] Tutorial Island complete.")
        }

        val microBreaks = MicroBreaks()

        TutIslandPaint(
            statusProvider = {
                if (microBreaks.isBreaking) "Micro break" else scheduler.currentTask?.displayName ?: "Idle"
            },
            startTime = startTimeMs
        ).install()

        Log.info(
            "[TutIsland] Starting | ironmanMode=${Settings.ironmanMode} " +
                    "walkLocation=${Settings.walkLocation}"
        )

        Camera.setRotationMethod(Camera.RotationMethod.MOUSE)

        while (!scheduler.stopRequested) {
            if (!Login.isLoggedIn()) {
                Waiting.waitUntil(5000) { Login.isLoggedIn() }
                Waiting.wait(1000)
                continue
            }

            // Player stuck watchdog
            updateMovementTimer()
            if (System.currentTimeMillis() - lastMoveMs >= noMovementTimeoutMs) {
                Log.warn("[TutIsland] No movement detected for 5 minutes. Stopping script.")
                break
            }

            // 80% short breaks 20% longer breaks
            if (microBreaks.tick()) {
                continue
            }

            scheduler.tick()
            Waiting.wait(TribotRandom.uniform(50, 200))
        }

        if (scheduler.stopRequested) {
            Login.logout()
        }

        Log.info("[TutIsland] Script finished.")
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
            Log.warn("[TutIsland] Invalid arg value(s): ${result.invalidValues.joinToString(", ")}")
        }

        Log.info(
            "[TutIsland] Loaded inline args | ironmanMode=${Settings.ironmanMode} " +
                "walkLocation=${Settings.walkLocation}"
        )
        return true
    }

    private fun loadProfile(profileArg: String, store: ProfileStore): Boolean {
        val profile = profileArg.trim()
            .removeSuffix(".properties")
            .removeSuffix(".json")

        val loaded = store.load(profile)
        if (loaded == null) {
            Log.error("[TutIsland] Profile not found: \"$profile\"")
            return false
        }

        Settings.fromSerializable(loaded)
        Log.info("[TutIsland] Loaded profile \"$profile\"")
        return true
    }

    private fun updateMovementTimer() {
        val currentTile = MyPlayer.getTile()

        if (lastPlayerTile == null) {
            lastPlayerTile = currentTile
            lastMoveMs = System.currentTimeMillis()
            return
        }

        if (currentTile != lastPlayerTile) {
            lastPlayerTile = currentTile
            lastMoveMs = System.currentTimeMillis()
        }
    }
}
