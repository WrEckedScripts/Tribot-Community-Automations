# tribot-community-scripts

Open-source scripts, automation plugins, and shared libraries for
[Tribot](https://tribot.org), contributed by the community and maintained
alongside the Tribot team.

Every top-level directory here is an independent Gradle subproject. Some are real
scripts you can build and run. Others are shared libraries that other modules depend
on. And a couple (`example-*`) are starter templates you can copy as the basis of a
new contribution.

---

## Getting started

You need:

- **JDK 21** (the Gradle wrapper will auto-provision one via Foojay toolchains if you
  don't have it locally)
- A local **Tribot** install — only required if you want to actually run what you
  build. Just compiling doesn't need Tribot.

Clone and build:

```bash
git clone https://github.com/TribotRS/tribot-community-scripts.git
cd tribot-community-scripts
./gradlew build
```

Deploy every script/plugin in the repo to your local Tribot automations directory:

```bash
./gradlew deployLocally
```

Deploy just a single module:

```bash
./gradlew :example-sdk-script:deployLocally
```

The `deployLocally` task is provided by the [`tribot-dev-plugin`][dev-plugin] and
drops a fat JAR into the platform-specific automations directory:

- **macOS**: `~/Library/Application Support/tribot/automations`
- **Windows**: `%APPDATA%/.tribot/automations`
- **Linux**: `~/.tribot/automations`

[dev-plugin]: https://github.com/TribotRS/tribot-dev-plugin

---

## Repository layout

```
tribot-community-scripts/
├── community-commons/              shared helper library (bundled into consumers)
├── example-automation-plugin/      automation plugin + companion script
├── build.gradle.kts                root build — applies Kotlin JVM + repositories
├── settings.gradle.kts             lists every subproject + plugin resolution
└── gradle.properties               Kotlin version pin
```

---

## Adding a new module

The short version:

1. Copy one of the `example-*` directories to a new name that describes your script
   (e.g. `my-awesome-woodcutter`).
2. Edit `build.gradle.kts` in the copy — rename the `register("...")` key, update
   `className`, `scriptName`, `author`, `description`, and `category`.
3. Rename the source file and update its `@TribotScriptManifest` / `@ScriptManifest`.
4. Add `include("my-awesome-woodcutter")` to the bottom of the root
   `settings.gradle.kts`.
5. Run `./gradlew :my-awesome-woodcutter:build` to make sure it compiles.
6. Open a pull request — see [`CONTRIBUTING.md`](./CONTRIBUTING.md) for the full
   review checklist.

---

## Using the shared library

The `community-commons` module holds helpers that make sense across many scripts.
To use it from your module, add it as a `bundled` dependency so its classes are
packed into your deploy jar:

```kotlin
dependencies {
    bundled(project(":community-commons"))
}
```

If you find yourself copy-pasting the same helper into three different scripts, that's
usually a signal it belongs in `community-commons`. Open a PR with the helper *and*
the scripts that use it so reviewers can see both sides.

---

## Gotchas

- **`bundled` vs `implementation`** — in script/plugin modules, use `bundled(...)` for
  any 3rd-party library Tribot doesn't already provide at runtime. Plain
  `implementation(...)` won't end up in the deploy jar, so your script will fail with
  `ClassNotFoundException` when it runs inside Tribot. See the
  [`tribot-dev-plugin` README][dev-plugin] for the full table.
- **Kotlin version** — pinned to `2.1.21` in `gradle.properties` to match the Tribot
  runtime. Don't bump it without also bumping the runtime.
- **Script manifest metadata** — `scriptName` is the display name shown in the Tribot
  UI; the Gradle `register("key")` key is just an internal name.

---

## License

See [`LICENSE`](./LICENSE). Contributions are accepted under the same license as the
rest of the repo; opening a PR constitutes agreement to that licensing.
