package org.tribot.community.examples.automation

import org.tribot.automation.TribotScript
import org.tribot.automation.script.ScriptContext


/**
 * A tiny script bundled alongside [ExampleAutomationPlugin] to show that a single
 * automation JAR can ship both scripts AND plugins.
 */
class ExampleAutomationScript : TribotScript {
    override fun execute(context: ScriptContext) {
    }
}
