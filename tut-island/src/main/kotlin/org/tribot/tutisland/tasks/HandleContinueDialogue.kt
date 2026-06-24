package org.tribot.tutisland.tasks

import org.tribot.script.sdk.ChatScreen
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.Widgets
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class HandleContinueDialogue: Task {
    override val displayName = "Handling Dialogue"
    override val priority = 10

    override fun canRun(): Boolean =
        ChatScreen.isClickContinueOpen() ||
                isCantReachMessageOpen() ||
                isSomeoneElseFightingMessageOpen() ||
                isNothingInterestingMessageOpen() ||
                isQuestCompleteContinueOpen()

    override fun execute() {
        if (ChatScreen.clickContinue()) {
            Waiting.waitUntil(TutPreferences.shortDelayMs()) {
                !ChatScreen.isClickContinueOpen() &&
                        !isCantReachMessageOpen() &&
                        !isSomeoneElseFightingMessageOpen() &&
                        !isNothingInterestingMessageOpen() &&
                        !isQuestCompleteContinueOpen()
            }
            return
        }

        if (isCantReachMessageOpen() || isSomeoneElseFightingMessageOpen() || isNothingInterestingMessageOpen()) {
            Widgets.get(162, 43).ifPresent { it.click() }
            Waiting.waitUntil(TutPreferences.shortDelayMs()) {
                !isCantReachMessageOpen() &&
                        !isSomeoneElseFightingMessageOpen() &&
                        !isNothingInterestingMessageOpen()
            }
            return
        }

        if (isQuestCompleteContinueOpen()) {
            Query.widgets()
                .textContains("Click here to continue")
                .isVisible()
                .findFirst()
                .ifPresent { it.click() }
            Waiting.waitUntil(TutPreferences.shortDelayMs()) { !isQuestCompleteContinueOpen() }
        }
    }

    private fun isCantReachMessageOpen(): Boolean =
        Widgets.get(162, 43)
            .map { it.isVisible && it.text.orElse("").contains("I can't reach") }
            .orElse(false)

    private fun isSomeoneElseFightingMessageOpen(): Boolean =
        Widgets.get(162, 43)
            .map { it.isVisible && it.text.orElse("").contains("Someone else is fighting", ignoreCase = true) }
            .orElse(false)

    private fun isNothingInterestingMessageOpen(): Boolean =
        Widgets.get(162, 43)
            .map { it.isVisible && it.text.orElse("").contains("Nothing interesting happens", ignoreCase = true) }
            .orElse(false)

    private fun isQuestCompleteContinueOpen(): Boolean =
        Query.widgets()
            .textContains("Congratulations, you've completed a quest")
            .isVisible()
            .isAny
}
