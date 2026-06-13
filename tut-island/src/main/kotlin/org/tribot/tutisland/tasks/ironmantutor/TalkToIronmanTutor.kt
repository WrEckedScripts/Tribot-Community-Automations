package org.tribot.tutisland.tasks.ironmantutor

import org.tribot.script.sdk.ChatScreen
import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.util.Walker
import org.tribot.tutisland.gui.Settings
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.data.IronmanMode
import org.tribot.tutisland.util.taskmanagement.Task

class TalkToIronmanTutor: Task {
    override val displayName = "Talking To Ironman Tutor"
    override val priority = 0

    override fun canRun(): Boolean {
        val desiredAccountType = desiredAccountType() ?: return false
        val ironmanSetupClosed = !Query.widgets().textContains("Ironman Setup").isAny

        return MyPlayer.getAccountType() != desiredAccountType &&
                GameState.getSetting(281) == 620 &&
                ironmanSetupClosed &&
                !ChatScreen.isClickContinueOpen()
    }

    override fun execute() {
        val npcName = "Ironman tutor"

        if (!Query.npcs().nameEquals(npcName).isReachable.isAny) {
            Walker.walkTo(Constants.ironmanTutorArea.randomTile)
            return
        }

        if (dialogueOptionsAvailable()) {
            if (dialogueOptions().any { it.contains("Ironmen") }) {
                chooseOptionContaining("Ironmen")
                Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                    !dialogueOptionsAvailable() || ChatScreen.isClickContinueOpen()
                }
                return
            }

            if (dialogueOptions().any { it.contains("Ironman") }) {
                chooseOptionContaining("Ironman")
                Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
                    !dialogueOptionsAvailable() || ChatScreen.isClickContinueOpen()
                }
                return
            }
        }

        if (GameState.getVarbit(12393) == 0) {
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

    private fun desiredAccountType(): MyPlayer.AccountType? =
        when (Settings.ironmanMode) {
            IronmanMode.IRON -> MyPlayer.AccountType.IRONMAN
            IronmanMode.HCIM -> MyPlayer.AccountType.HARDCORE_IRONMAN
            IronmanMode.UIM -> MyPlayer.AccountType.ULTIMATE_IRONMAN
            IronmanMode.GROUP_IRONMAN -> MyPlayer.AccountType.GROUP_IRONMAN
            IronmanMode.GROUP_HCIM -> MyPlayer.AccountType.HARDCORE_GROUP_IRONMAN
            IronmanMode.STANDARD -> null
        }
}