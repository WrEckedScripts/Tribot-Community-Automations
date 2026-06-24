package org.tribot.tutisland.tasks.ironmantutor

import org.tribot.automation.script.core.widgets.PinScreen
import org.tribot.script.sdk.AutomationSdk
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.tutisland.gui.Settings
import org.tribot.tutisland.util.TutPreferences
import org.tribot.tutisland.util.data.Constants
import org.tribot.tutisland.util.data.IronmanMode
import org.tribot.tutisland.util.taskmanagement.Task

class SelectIronmanMode: Task {
    override val displayName = "Selecting Ironman Mode"
    override val priority = 0

    override fun canRun(): Boolean {
        val desiredAccountType = desiredAccountType() ?: return false

        if (isPinSetupPromptOpen() || AutomationSdk.getContext().pinScreen.isOpen()) {
            return true
        }

        return isSetupVisible() && MyPlayer.getAccountType() != desiredAccountType
    }

    override fun execute() {
        val (optionChild, action) = when (Settings.ironmanMode) {
            IronmanMode.IRON -> Constants.IRONMAN to "Ironman"
            IronmanMode.HCIM -> Constants.HCIM to "Hardcore Ironman"
            IronmanMode.UIM -> Constants.ULTIMATE to "Ultimate Ironman"
            IronmanMode.GROUP_IRONMAN -> Constants.GROUP to "Group Ironman"
            IronmanMode.GROUP_HCIM -> Constants.HCGROUP to "Hardcore Group Ironman"
            IronmanMode.STANDARD -> {
                return
            }
        }

        if (handlePinSetup()) {
            return
        }

        Log.info("[TutIsland] Selecting ${Settings.ironmanMode} mode.")
        if (!clickWidget(890, optionChild, action = action)) {
            Log.warn("[TutIsland] Failed to click ${Settings.ironmanMode} mode.")
            return
        }

        Waiting.waitUntil(TutPreferences.longDelayMs()) {
            AutomationSdk.getContext().pinScreen.isOpen() || isPinSetupPromptOpen()
        }
        handlePinSetup()
    }

    private fun isSetupVisible(): Boolean =
        listOf(Constants.IRONMAN, Constants.ULTIMATE, Constants.HCIM, Constants.GROUP, Constants.HCGROUP).any { child ->
            Query.widgets().inIndexPath(890, child).isVisible().isAny
        }

    private fun handlePinSetup(): Boolean {
        val pinScreen = AutomationSdk.getContext().pinScreen
        val pinSetup = isPinSetupPromptOpen()

        if (!pinSetup && !pinScreen.isOpen()) {
            return false
        }

        if (pinSetup) {
            if (!clickWidget(289, 8, 8, action = "Proceed")) {
                return true
            }

            Waiting.waitUntil(TutPreferences.longDelayMs()) {
                pinScreen.isOpen()
            }
        }

        enterPin(pinScreen)
        return true
    }

    private fun clickWidget(vararg indexPath: Int, action: String? = null): Boolean {
        val widget = Query.widgets()
            .inIndexPath(*indexPath)
            .isVisible()
            .findFirst()
            .orElse(null)
            ?: return false

        widget.scrollTo()
        return if (action == null) widget.click() else widget.click(action)
    }

    private fun isPinSetupPromptOpen(): Boolean =
        Query.widgets().textContains("Would you like to set a PIN").isAny

    private fun enterPin(pinScreen: PinScreen): Boolean {
        if (!pinScreen.isOpen()) {
            return false
        }

        pinScreen.enterPin()

        return !pinScreen.isOpen()
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
