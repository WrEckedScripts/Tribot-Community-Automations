plugins {
    kotlin("jvm")
    id("org.tribot.dev") version "latest.release"
}

tribot {
    useCompose = false
    useJavaFx = false

    scripts {
        register("wrblastpumper") {
            className = "org.tribot.wrblastpumper.WrBlastPumper"
            scriptName = "WrBlastPumper"
            version = "1.0.0"
            author = "WrEcked"
            description = "Operates the Blast Furnace pump for AFK Strength experience"
            category = "strength"
        }
    }
}

dependencies {
    bundled(project(":wrscript-utilities"))
    bundled("com.github.Nullable-TB:nullable-lib:latest.release")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
