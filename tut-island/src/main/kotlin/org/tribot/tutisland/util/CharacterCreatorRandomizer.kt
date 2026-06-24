package org.tribot.tutisland.util

import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.Widgets
import kotlin.random.Random

object CharacterCreatorRandomizer {

    data class PairButtons(val left: WidgetRef, val right: WidgetRef)
    data class WidgetRef(val parent: Int, val child: Int, val sub: Int? = null)

    enum class Direction { LEFT, RIGHT }

    private fun getWidget(ref: WidgetRef) =
        if (ref.sub == null) Widgets.get(ref.parent, ref.child).orElse(null)
        else Widgets.get(ref.parent, ref.child, ref.sub).orElse(null)

    fun click(ref: WidgetRef): Boolean {
        val w = getWidget(ref) ?: return false
        if (!w.isVisible) {
            return false
        }
        val ok = w.click()
        return ok
    }

    fun clickPair(
        pair: PairButtons,
        dir: Direction,
        steps: Int,
        rng: Random
    ) {
        // Click the same arrow a random number of times. The game cycles through
        // the available styles for us, so we do not need to know every style ID.
        val ref = when (dir) {
            Direction.RIGHT -> pair.right
            Direction.LEFT  -> pair.left
        }

        repeat(steps) {
            click(ref)
            Waiting.wait((TutPreferences.shortDelayMs().toLong() + rng.nextLong(0, 180)).toInt())
        }
    }

    fun randDir(rng: Random): Direction =
        if (rng.nextBoolean()) Direction.LEFT else Direction.RIGHT

    fun randSteps(rng: Random, min: Int, max: Int): Int =
        rng.nextInt(min, max + 1)
}
