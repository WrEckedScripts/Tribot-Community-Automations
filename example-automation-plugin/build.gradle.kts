plugins {
    kotlin("jvm")
    id("org.tribot.dev") version "latest.release"
}

// An "automation plugin" is a long-lived piece of code that loads inside Tribot Echo
// and can expose callable functions over the automation API, subscribe to game tick
// events, emit events, and inspect runtime state. Unlike scripts (which run once and
// finish), plugins start when the runtime boots and stop when it shuts down.
//
// A single module can declare both scripts AND plugins — that's what this example
// does. The plugin registers a few callable helpers and the SDK script kicks the tires
// on them from a regular script lifecycle.
tribot {
    useCompose = false
    useJavaFx = false

    scripts {
        register("exampleAutomationScript") {
            className = "org.tribot.community.examples.automation.ExampleAutomationScript"
            scriptName = "Example"
            version = "1.0.0"
            author = "Tribot Community"
            description = "Smoke-tests the functions registered by the example automation plugin."
            category = "Examples"
        }
    }

    automationPlugins {
        register("exampleAutomationPlugin") {
            className = "org.tribot.community.examples.automation.ExampleAutomationPlugin"
            pluginName = "Example: Automation Plugin"
            version = "1.0.0"
        }
    }
}

dependencies {
    bundled(project(":community-commons"))
}
