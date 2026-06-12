plugins {
    kotlin("jvm")
    id("org.tribot.dev") version "latest.release"
}

tribot {
    useCompose = false
    useJavaFx = false
}

dependencies {
    compileOnly("com.github.Nullable-TB:nullable-lib:latest.release")
}
