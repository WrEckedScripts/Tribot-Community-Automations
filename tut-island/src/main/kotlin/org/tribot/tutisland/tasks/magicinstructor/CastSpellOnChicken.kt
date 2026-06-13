package org.tribot.tutisland.tasks.magicinstructor

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Magic
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.Npc
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task
import kotlin.random.Random

class CastSpellOnChicken: Task {
    override val displayName = "Attacking Chicken"
    override val priority = 0

    private val rng = Random.Default
    private var castConfirmed = false

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 650 &&
                !castConfirmed
    }

    override fun execute() {
        if (!Constants.magicInstructorChickenArea.contains(MyPlayer.getTile())) {
            Walker.walkTo(Constants.magicInstructorChickenArea.randomTile)
            return
        }

        val chicken = pickRandomFreeChicken(rng) ?: return
        val initialHealthPercent = chicken.healthBarPercent

        if (Magic.castOn("Wind Strike", chicken)) {
            castConfirmed = Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                isCastConfirmed(chicken, initialHealthPercent)
            }
        }
    }

    private fun isCastConfirmed(chicken: Npc, initialHealthPercent: Double): Boolean =
        MyPlayer.get()
            .map { player -> player.interactingCharacter.orElse(null) == chicken }
            .orElse(false) ||
                chicken.isInteracting ||
                chicken.isHealthBarVisible ||
                chicken.healthBarPercent != initialHealthPercent

    private fun pickRandomFreeChicken(rng: Random) =
        Query.npcs()
            .nameEquals("Chicken")
            .toList()
            .filter { !it.isInteracting }
            .filter { it.interactingCharacter.orElse(null) == null }
            .filter { !it.isHealthBarVisible }
            .randomOrNull()
}