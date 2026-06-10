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
        register("playermover") {
            className = "org.tribot.playermover.PlayerMover"
            scriptName = "Player Mover"
            version = "1.0.0"
            author = "WrEcked"
            description = "Allows you to move your player around the map\n" +
                    "Arguments supported: location OR x, y, plane and logout\n\n" +
                    "Example: location:3183,3440,0|logout:false -> moves the player to the Varrock West Bank"
            category = "utilities"
        }
    }
}

dependencies {
    bundled(project(":community-commons"))
}
