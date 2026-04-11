package org.tribot.community.examples.automation

import org.tribot.community.commons.AutomationHelpers
import org.tribot.automation.GameListener
import org.tribot.automation.TribotAutomationApi
import org.tribot.automation.TribotPlugin
import org.tribot.automation.TribotPluginContext
import org.tribot.automation.TribotPluginLogger

/**
 * A minimal automation plugin. Automation plugins are long-lived components that load
 * with Tribot and can:
 *   - Register callable functions other tools can invoke over the automation API
 *   - Subscribe to game tick events to observe client state in real time
 *   - Inspect what scripts are running, start/stop/pause them, etc.
 *
 * This one registers two callable functions — `community_hello` and `community_status`
 * — and logs a line every 100 game ticks so you can see the tick listener firing in
 * the log pane.
 */
class ExampleAutomationPlugin : TribotPlugin {

    private var api: TribotAutomationApi? = null
    private var logger: TribotPluginLogger? = null
    private var tickListener: GameListener? = null
    private var tickCount: Long = 0

    override fun start(context: TribotPluginContext) {
        val api = context.automationApi
        val logger = context.logger

        this.api = api
        this.logger = logger

        logger.info("ExampleAutomationPlugin starting")

        // Simple callable — takes an optional name and returns a greeting.
        api.registerFunction("community_hello") { input ->
            val who = input?.takeIf { it.isNotBlank() } ?: "world"
            "Hello, $who! — from ExampleAutomationPlugin"
        }

        // Uses the shared helper from community-commons to render a status blurb.
        api.registerFunction("community_status") {
            val running = AutomationHelpers.describeRunningScript(api)
            val scripts = AutomationHelpers.renderScriptList(api).ifBlank { "<no scripts>" }
            "Currently running: $running\n\nAvailable scripts:\n$scripts"
        }

        // Tick listener — fires on every game tick. Keep the body cheap; anything
        // expensive should dispatch to a background thread.
        val listener: GameListener = {
            tickCount += 1
            if (tickCount % 100L == 0L) {
                logger.debug("ExampleAutomationPlugin: $tickCount ticks observed")
            }
        }
        tickListener = listener
        api.registerTickListener(listener)

        logger.info("ExampleAutomationPlugin ready — registered: community_hello, community_status")
    }

    override fun stop() {
        logger?.info("ExampleAutomationPlugin stopping")

        tickListener?.let { api?.unregisterTickListener(it) }
        api?.unregisterFunction("community_hello")
        api?.unregisterFunction("community_status")

        tickListener = null
        api = null
        logger = null
        tickCount = 0
    }
}
