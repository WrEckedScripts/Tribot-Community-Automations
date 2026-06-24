package org.tribot.wrdefriender.tasks

import net.runelite.api.gameval.InterfaceID
import net.runelite.api.widgets.Widget
import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.widgets.GameTab
import org.tribot.wrdefriender.DefrienderState
import org.tribot.wrdefriender.contracts.TaskContract
import org.tribot.script.sdk.Waiting as SdkWaiting

class RemoveFriendTask(private val ctx: ScriptContext) : TaskContract {
    override val name = "Removing friend"

    override fun perform(): Boolean {
        if (!ctx.tabs.open(GameTab.FRIENDS)) return false

        val list = waitForFriendsList() ?: return false
        val friend = list.dynamicChildren.firstOrNull { it.removeAction() != null } ?: return false
        val action = friend.removeAction() ?: return false
        val countBefore = ctx.client.friendContainer.count

        if (!ctx.interaction.scrollChildIntoView(list, friend)) return false
        if (!ctx.interaction.click(friend, action)) return false

        val removed = SdkWaiting.waitUntil(3_000) {
            ctx.client.friendContainer.count < countBefore
        }
        if (removed) DefrienderState.removed++
        return removed
    }

    private fun waitForFriendsList(): Widget? {
        var list: Widget? = null
        SdkWaiting.waitUntil(5_000) {
            list = ctx.client.getWidget(InterfaceID.Friends.LIST)
            list?.isHidden == false
        }
        return list
    }

    private fun Widget.removeAction(): String? =
        actions?.firstOrNull {
            it.equals("Remove", ignoreCase = true) || it.equals("Delete", ignoreCase = true)
        }
}
