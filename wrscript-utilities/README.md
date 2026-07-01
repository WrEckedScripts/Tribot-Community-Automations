# WrScript Utilities

Shared components for WrEcked scripts.

```kotlin
WrScriptHud(
    scriptName = "WrExample",
    skill = Skill.STRENGTH,
    accent = Colors.success,
    panelFill = Colors.alpha(Colors.panelBg, 235),
    labelColor = Colors.dim,
    valueColor = Colors.white,
)
    .row("Next action") { nextActionText() }
    .row("Items made") { itemsMade.toString() }
    .install()
```

Custom rows are evaluated every paint frame and automatically expand the panel
and its protected mouse bounds.
