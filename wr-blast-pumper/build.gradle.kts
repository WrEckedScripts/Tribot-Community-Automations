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
            description = "" +
                    "Operates the Blast Furnace pump for AFK Strength experience \n" +
                    "Features: \n" +
                    " - Refueling the stove (refuel:false to disable this) \n" +
                    " - Custom world (world:X to support your world) \n"
            category = "combat"
        }
    }
}

dependencies {
    bundled(project(":community-commons"))
    bundled(project(":wrscript-utilities"))
    bundled("com.github.Nullable-TB:nullable-lib:latest.release")
}

tasks.test {
    useJUnitPlatform()
}
