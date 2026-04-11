package org.tribot.community.commons

import org.tribot.automation.TribotAutomationApi

/**
 * Utilities built on top of [TribotAutomationApi]. These are intended for use from
 * automation plugins (TribotPlugin implementations), which have access to the
 * automation API via their plugin context.
 */
object AutomationHelpers {

    /**
     * Renders a one-line human-readable summary of the currently running script,
     * or "idle" if nothing is running.
     */
    fun describeRunningScript(api: TribotAutomationApi): String {
        val running = api.getRunningScript() ?: return "idle"
        val paused = if (running.isPaused) " (paused)" else ""
        return "${running.name}$paused"
    }

    /**
     * Lists every script available to the runtime as `name vX.Y.Z` lines, newline-separated.
     * Returns a blank string when the script list is empty rather than an empty multiline
     * (so calling `.isBlank()` at the call site is a valid "empty?" check).
     */
    fun renderScriptList(api: TribotAutomationApi): String {
        val scripts = api.getScripts()
        if (scripts.isEmpty()) return ""
        return scripts.joinToString("\n") { "${it.name} v${it.version}" }
    }
}
