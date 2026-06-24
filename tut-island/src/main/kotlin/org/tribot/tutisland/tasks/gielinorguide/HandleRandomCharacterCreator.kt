package org.tribot.tutisland.tasks.gielinorguide

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.Widgets
import org.tribot.tutisland.util.CharacterCreatorRandomizer
import org.tribot.tutisland.util.CharacterCreatorRandomizer.PairButtons
import org.tribot.tutisland.util.CharacterCreatorRandomizer.WidgetRef
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task
import kotlin.random.Random

class HandleRandomCharacterCreator(private val rng: Random = Random.Default): Task {

    override val displayName = "Randomizing Character"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 1 &&
                Widgets.get(679, 5).map { it.isVisible }.orElse(false)

    }

    override fun execute() {
        // Each character part has left and right arrow buttons. We click a random
        // arrow a few times so every account starts with a different look.
        val parts = listOf(
            "Head" to PairButtons(WidgetRef(679, 15), WidgetRef(679, 16)),
            "Jaw" to PairButtons(WidgetRef(679, 19), WidgetRef(679, 20)),
            "Torso" to PairButtons(WidgetRef(679, 23), WidgetRef(679, 24)),
            "Arms" to PairButtons(WidgetRef(679, 27), WidgetRef(679, 28)),
            "Hands" to PairButtons(WidgetRef(679, 31), WidgetRef(679, 32)),
            "Legs" to PairButtons(WidgetRef(679, 35), WidgetRef(679, 36)),
            "Feet" to PairButtons(WidgetRef(679, 39), WidgetRef(679, 40))
        )

        val colors = listOf(
            "HairColor" to PairButtons(WidgetRef(679, 46), WidgetRef(679, 47)),
            "TorsoColor" to PairButtons(WidgetRef(679, 50), WidgetRef(679, 51)),
            "LegColor" to PairButtons(WidgetRef(679, 54), WidgetRef(679, 55)),
            "FeetColor" to PairButtons(WidgetRef(679, 58), WidgetRef(679, 59)),
            "SkinColor" to PairButtons(WidgetRef(679, 62), WidgetRef(679, 63))
        )

        val bodyVar = GameState.getVarbit(14021)
        val wantA = rng.nextBoolean()

        // Body type is a separate choice from the arrow-button options above.
        // Only click it when the random choice is different from the current one.
        val shouldClick = (wantA && bodyVar != 0) || (!wantA && bodyVar != 1)

        if (shouldClick) {
            val bodyTypeRef = if (wantA) WidgetRef(679, 68) else WidgetRef(679, 69)
            CharacterCreatorRandomizer.click(bodyTypeRef)

            Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                GameState.getVarbit(14021) == if (wantA) 0 else 1
            }
        } else {
            Waiting.wait(TutPreferences.shortDelayMs())
        }

        for ((_, pair) in parts) {
            val dir = CharacterCreatorRandomizer.randDir(rng)
            val steps = CharacterCreatorRandomizer.randSteps(rng, min = 0, max = 4)
            CharacterCreatorRandomizer.clickPair(pair, dir, steps, rng)
            Waiting.wait(TutPreferences.shortDelayMs())
        }

        for ((_, pair) in colors) {
            val dir = CharacterCreatorRandomizer.randDir(rng)
            val steps = CharacterCreatorRandomizer.randSteps(rng, min = 0, max = 5)
            CharacterCreatorRandomizer.clickPair(pair, dir, steps, rng)
            Waiting.wait(TutPreferences.shortDelayMs())
        }

        val clicked = CharacterCreatorRandomizer.click(WidgetRef(679, 74))

        if (clicked) {
            Waiting.waitUntil(TutPreferences.longDelayMs()) {
                Widgets.get(679, 74).map { !it.isVisible }.orElse(true)
            }
        }
    }
}
