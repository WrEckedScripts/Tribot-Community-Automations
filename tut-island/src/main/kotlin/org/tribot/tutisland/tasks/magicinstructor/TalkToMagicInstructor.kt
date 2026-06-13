package org.tribot.tutisland.tasks.magicinstructor

import org.tribot.script.sdk.ChatScreen
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.Widgets
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.taskmanagement.Task

class TalkToMagicInstructor: Task {
    override val displayName = "Talking To Magic Instructor"
    override val priority = 0

    override fun canRun(): Boolean {
        val step = GameState.getSetting(281)

        return step == 620 ||
                step == 640 ||
                step in 670..680 ||
                magicInstructorOptionsAvailable(step)

    }

    override fun execute() {
        val npcName = "Magic Instructor"

        if (!Query.npcs().nameEquals(npcName).isReachable.isAny) {
            Walker.walkTo(Constants.magicInstructorArea.randomTile)
            return
        }

        if (dialogueOptionsAvailable()) {
            if (dialogueOptions().any { it.contains("Yes, I'd like to go to the mainland") }) {
                chooseOptionContaining("Yes, I'd like to go to the mainland")
                Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                    !dialogueOptionsAvailable() || ChatScreen.isClickContinueOpen()
                }
                return
            }

            if (dialogueOptions().any { it.contains("Ironman") }) {
                chooseOptionContaining("No, I'm not planning to do that.")
                Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                    !dialogueOptionsAvailable() || ChatScreen.isClickContinueOpen()
                }
                return
            }

            if (dialogueOptions().any { it.contains("Yes") }) {
                chooseOptionContaining("Yes")
                Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                    !dialogueOptionsAvailable() || ChatScreen.isClickContinueOpen()
                }
                return
            }

            chooseFirstOption()
            Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                !dialogueOptionsAvailable() || ChatScreen.isClickContinueOpen()
            }
            return
        }

        Widgets.get(153, 16).ifPresent { if (it.isVisible) it.click() }
        Widgets.get(890, 2, 11).ifPresent { if (it.isVisible) it.click() }

        if (GameState.getVarbit(12393) == 0 && !ChatScreen.isClickContinueOpen()) {
            Query.npcs()
                .nameEquals(npcName)
                .actionEquals("Talk-to")
                .isReachable
                .findBestInteractable()
                .ifPresent { npc ->
                    if (npc.interact("Talk-to")) {
                        Waiting.waitUntil(TutPreferences.longDelayMs()) {
                            ChatScreen.isClickContinueOpen()
                        }
                    }
                }
        }
    }

    private fun dialogueOptionsAvailable(): Boolean =
        ChatScreen.isOpen() && !ChatScreen.isClickContinueOpen()

    private fun magicInstructorOptionsAvailable(step: Int): Boolean {
        if (step < 620 || !dialogueOptionsAvailable()) return false

        return dialogueOptions().any { it.contains("Yes, I'd like to go to the mainland") } ||
                dialogueOptions().any { it.contains("Ironman") } ||
                dialogueOptions().any { it == "Yes" }
    }

    private fun dialogueOptions(): List<String> =
        Query.widgets()
            .isVisible()
            .toList()
            .mapNotNull { widget -> widget.text.orElse("").takeIf { it.isNotBlank() } }

    private fun chooseOptionContaining(text: String): Boolean {
        val option = dialogueOptions().firstOrNull { it.contains(text) }
            ?: return false

        return ChatScreen.selectOption(option)
    }

    private fun chooseFirstOption(): Boolean {
        val option = dialogueOptions().firstOrNull() ?: return false
        return ChatScreen.selectOption(option)
    }
}