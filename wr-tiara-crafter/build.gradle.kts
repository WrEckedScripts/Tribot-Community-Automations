plugins {
    kotlin("jvm")
    id("org.tribot.dev") version "latest.release"
}

tribot {
    useCompose = false
    useJavaFx = false

    scripts {
        register("wrtiaracrafter") {
            className = "org.tribot.wrtiaracrafter.WrTiaraCrafter"
            scriptName = "WrTiaraCrafter"
            version = "1.0.0"
            author = "WrEcked"
            description = "Crafts air tiara's to train Runecrafting"
            category = "runecrafting"
        }
    }
}

dependencies {
    bundled(project(":community-commons"))
    bundled("com.github.Nullable-TB:nullable-lib:latest.release")
}
