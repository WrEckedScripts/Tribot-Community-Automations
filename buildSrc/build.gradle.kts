// Build logic for the website release pipeline. Lives in buildSrc so the root
// build script stays a thin wiring layer over `ReleaseScriptsTask`.

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    // The release task talks JSON to the backend; the JDK has an HTTP client but no
    // JSON support, and gson is small enough to not slow down build-logic compilation.
    implementation("com.google.code.gson:gson:2.11.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
