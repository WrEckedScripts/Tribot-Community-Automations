plugins {
    kotlin("jvm")
    id("org.tribot.dev") version "latest.release"
}

tribot {
    useCompose = false
    useJavaFx = false

    scripts {
        register("wrdefriender") {
            className = "org.tribot.wrdefriender.WrDefriender"
            scriptName = "WrDefriender"
            version = "1.0.0"
            author = "WrEcked"
            description = "Removes every player from the account's friends list"
            category = "tools"
        }
    }
}

dependencies {
    bundled("com.github.Nullable-TB:nullable-lib:latest.release")
}
