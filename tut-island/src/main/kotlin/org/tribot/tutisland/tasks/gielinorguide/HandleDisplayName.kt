package org.tribot.tutisland.tasks.gielinorguide

import org.tribot.script.sdk.GameState
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.Widgets
import org.tribot.script.sdk.input.Keyboard
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.Widget
import org.tribot.tutisland.util.OsrsNameGenerator
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.taskmanagement.Task

class HandleDisplayName: Task {
    override val displayName = "Setting Display Name"
    override val priority = 0

    override fun canRun(): Boolean {
        return GameState.getSetting(281) == 1 &&
                Widgets.get(558, 6).map { it.isVisible }.orElse(false)
    }

    override fun execute() {
        when (nameLookupState()) {
            NameLookupState.AVAILABLE -> confirmName()
            NameLookupState.NOT_AVAILABLE,
            NameLookupState.NEEDS_LOOKUP -> enterGeneratedName()
        }
    }

    private fun enterGeneratedName() {
        val enterNameWidget = getEnterNameWidget() ?: return

        if (enterNameWidget.click("Enter name")) {
            Waiting.wait(TutPreferences.shortDelayMs())
        }

        if (!isNameEntryReady()) {
            clearCurrentName()
        }

        typeGeneratedName()
    }

    private fun confirmName() {
        val enterNameWidget = getEnterNameWidget() ?: return
        val setNameWidget = Widgets.get(558, 19, 9).orElse(null) ?: return

        if (setNameWidget.click()) {
            Waiting.waitUntil(TutPreferences.longDelayMs() + TutPreferences.longDelayMs()) {
                !enterNameWidget.isVisible
            }
        }
    }

    private fun typeGeneratedName() {
        val previousMessage = messageTextClean()
        val generated = OsrsNameGenerator.generate()

        Keyboard.typeString(generated)
        Keyboard.pressEnter()

        Waiting.waitUntil(TutPreferences.longDelayMs()) {
            messageTextClean() != previousMessage
        }

        Waiting.waitUntil(TutPreferences.longDelayMs() + TutPreferences.longDelayMs()) {
            nameLookupState() != NameLookupState.NEEDS_LOOKUP
        }
    }

    private fun clearCurrentName() {
        val deleteCount = cleanWidgetText(558, 12)
            .length
            .coerceAtLeast(1)

        Keyboard.typeString("\b".repeat(deleteCount))
        Waiting.waitUntil(TutPreferences.mediumDelayMs()) {
            isNameEntryReady()
        }
    }

    private fun getEnterNameWidget(): Widget? =
        Query.widgets()
            .toList()
            .firstOrNull { widget ->
                widget.actions.any { action ->
                    action?.contains("Enter name") == true
                }
            }

    private fun nameLookupState(): NameLookupState {
        val message = messageTextClean()

        // After entering a name, the game explains whether it can be used in this
        // message box. Use that text to decide whether to try again or confirm.
        return when {
            message.contains("try clicking one of our suggestions") -> NameLookupState.NOT_AVAILABLE
            message.contains("you may set this name now") && !message.contains("please look up a name to see whether it is available") -> NameLookupState.AVAILABLE
            else -> NameLookupState.NEEDS_LOOKUP
        }
    }

    private fun isNameEntryReady(): Boolean {
        val text = cleanWidgetText(558, 12)
        return text.isBlank() || text.all { it == '*' }
    }

    private fun messageTextClean(): String =
        cleanWidgetText(558, 13).lowercase()

    private fun cleanWidgetText(root: Int, child: Int): String =
        Widgets.get(root, child)
            .flatMap { it.text }
            .orElse("")
            .replace(Regex("<.*?>"), "")
            .trim()

    private enum class NameLookupState {
        AVAILABLE,
        NOT_AVAILABLE,
        NEEDS_LOOKUP
    }
}
