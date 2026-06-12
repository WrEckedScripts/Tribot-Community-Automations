package org.tribot.community.commons.randomization

import org.tribot.automation.script.ScriptContext
import kotlin.random.Random

/**
 * Utility object to **randomize certain actions** based on a probability or range of probabilities.
 * Used for antiban-style randomization or lightweight behavioral variance in terms of anti-profiling.
 *
 * ### Usage examples
 *
 * #### 1. Fixed probability
 * Executes the block with a fixed 8% chance.
 * ```kotlin
 * Lottery.execute(0.08) {
 *     MiniBreak.fatigueLeave()
 * }
 * ```
 *
 * #### 2. Range-based probability
 * Randomly picks a probability between 5% and 13%, and executes based on that roll.
 * ```kotlin
 * Lottery.execute(0.05..0.13) {
 *     MiniBreak.fatigueLeave()
 * }
 * ```
 *
 * #### 3. Guaranteed condition
 * Always executes when a condition is met (e.g., inventory full),
 * otherwise rolls based on probability range.
 * ```kotlin
 * Lottery.executeGuaranteed(0.05..0.13, guaranteedCondition = { Inventory.isFull() }) {
 *     processInventory()
 * }
 * ```
 *
 * ### Notes
 * - All executions are **stateless**, meaning each call is independent.
 * - The [guaranteedCondition] allows forced execution without interfering with probability rolls.
 * - The probability values are expressed as decimals (e.g., `0.05` = 5%).
 */
object Lottery{
    private var enableLogging = false
    private lateinit var ctx: ScriptContext

    /**
     * Configures the Lottery, currently to enable/disable logging.
     *
     * @example Lottery.configure(ctx, true)
     */
    fun configure(context: ScriptContext,loggingEnabled: Boolean = false) {
        ctx = context
        enableLogging = loggingEnabled
    }

    /**
     * Executes the given [action] with a fixed [probability] chance.
     *
     * @param probability A value between `0.0` and `1.0`, representing the likelihood
     * that the [action] will be executed.
     * @param action The lambda to execute if the dice roll succeeds.
     */
    fun execute(probability: Double, action: () -> Unit) {
        val won = shouldExecute(probability)
        if (won) {
            trace { "[Lottery] - Executing randomized action" }
            action()
        }
    }

    /**
     * Executes the given [action] based on a probability randomly chosen from the specified [probabilityRange].
     *
     * @param probabilityRange The inclusive range (`start..endInclusive`) from which a random probability is drawn.
     * @param action The lambda to execute if the dice roll succeeds.
     */
    fun execute(probabilityRange: ClosedRange<Double>, action: () -> Unit) {
        val probability = Random.nextDouble(probabilityRange.start, probabilityRange.endInclusive)
        execute(probability, action)
    }

    /**
     * Executes the given [action] if [guaranteedCondition] returns `true`,
     * or based on a random [probability] otherwise.
     *
     * @param probability A value between `0.0` and `1.0`, representing the likelihood
     * that the [action] will be executed when not guaranteed.
     * @param guaranteedCondition A lambda returning `true` to force guaranteed execution
     * regardless of random outcome (e.g., when inventory is full).
     * @param action The lambda to execute when triggered.
     */
    fun executeGuaranteed(
        probability: Double,
        guaranteedCondition: () -> Boolean = { false },
        action: () -> Unit,
    ) {
        val won = guaranteedCondition() || shouldExecute(probability)
        if (won) {
            trace { "[Lottery] - Executing randomized action" }
            action()
        }
    }

    /**
     * Executes the given [action] based on a probability randomly chosen from the specified [probabilityRange],
     * or guarantees execution when [guaranteedCondition] is met.
     *
     * @param probabilityRange The inclusive range (`start..endInclusive`) from which a random probability is drawn.
     * @param guaranteedCondition A lambda returning `true` to force guaranteed execution
     * regardless of random outcome.
     * @param action The lambda to execute when triggered.
     */
    fun executeGuaranteed(
        probabilityRange: ClosedRange<Double>,
        guaranteedCondition: () -> Boolean = { false },
        action: () -> Unit,
    ) {
        val probability = Random.nextDouble(probabilityRange.start, probabilityRange.endInclusive)
        executeGuaranteed(probability, guaranteedCondition, action)
    }

    /**
     * Determines whether an action should be executed based on the provided [probability].
     *
     * @param probability A value between `0.0` and `1.0`.
     * @return `true` if the random roll is below the given [probability]; otherwise `false`.
     * @throws IllegalArgumentException if [probability] is outside the valid range.
     */
    private fun shouldExecute(probability: Double): Boolean {
        require(probability in 0.0..1.0) { "Probability must be between 0.0 and 1.0" }

        val diceRoll = Random.nextDouble() // Value between 0.0 and 1.0
        trace { "[Lottery] - Rolled: $diceRoll | Probability: $probability" }
        trace { "[Lottery] - Resulting in execution : ${diceRoll < probability}" }
        return diceRoll < probability
    }

    /**
     * Helpers
     */
    private inline fun trace(message: () -> String) {
        if (enableLogging) {
            ctx.logger.trace(message())
        }
    }
}
