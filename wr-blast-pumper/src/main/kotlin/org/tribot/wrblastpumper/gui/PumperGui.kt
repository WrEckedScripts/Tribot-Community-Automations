package org.tribot.wrblastpumper.gui

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.stage.Stage
import nullablelib.NullableLib
import org.tribot.wrscript.utilities.hud.WrScriptHud
import java.awt.Desktop
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class PumperGui(
    private val profileStore: PumperProfileStore,
) {
    fun showAndWait(initialSettings: PumperSettings): PumperSettings? {
        val completed = AtomicBoolean(false)
        val result = AtomicReference<PumperSettings?>()
        val completionLatch = CountDownLatch(1)

        val stage = GuiLauncher.create("WrBlastPumper GUI") { guiStage ->
            buildUi(
                stage = guiStage,
                initialSettings = initialSettings,
                onComplete = { settings ->
                    if (completed.compareAndSet(false, true)) {
                        settings?.installAsArguments()
                        result.set(settings)
                        completionLatch.countDown()
                    }
                },
            )
            guiStage.show()
            guiStage.centerOnScreen()
        }

        if (stage == null) return null

        try {
            completionLatch.await()
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            Platform.runLater { stage.close() }
            return null
        }

        return result.get()
    }

    private fun buildUi(
        stage: Stage,
        initialSettings: PumperSettings,
        onComplete: (PumperSettings?) -> Unit,
    ) {
        val profileField = TextField().apply {
            promptText = "Profile name"
            text = "lastrun"
            maxWidth = Double.MAX_VALUE
        }
        HBox.setHgrow(profileField, Priority.ALWAYS)

        HBox.setHgrow(profileField, Priority.ALWAYS)

        val loadButton = Button("Load")
        val saveButton = Button("Save")
        val profileRow = HBox(8.0, profileField, loadButton, saveButton).apply {
            alignment = Pos.CENTER_LEFT
        }

        val stoppingLevelField = TextField().apply {
            promptText = "Stop at level"
            text = initialSettings.stopAt.toString()
            maxWidth = Double.MAX_VALUE
        }

        val worldField = TextField().apply {
            promptText = "Optional"
            text = initialSettings.world?.toString().orEmpty()
            maxWidth = Double.MAX_VALUE
        }
        val refuelCheck = CheckBox("Enabled").apply {
            isSelected = initialSettings.refuel
        }

        val settingsGrid = GridPane().apply {
            hgap = 14.0
            vgap = 12.0
            add(Label("Custom world"), 0, 0)
            add(worldField, 1, 0)
            add(Label("Refuel stove"), 0, 1)
            add(refuelCheck, 1, 1)
            add(Label("Stop at level"), 0, 2)
            add(stoppingLevelField, 1, 2)

            GridPane.setHgrow(worldField, Priority.ALWAYS)
        }

        val content = VBox(
            10.0,
            Label("Profile").apply { styleClass += "section-title" },
            profileRow,
            Separator(),
            Label("Settings").apply { styleClass += "section-title" },
            settingsGrid,
        ).apply {
            padding = Insets(20.0)
            styleClass += "content"
        }

        val exitButton = Button("Exit").apply {
            styleClass += "exit-button"
        }
        val runButton = Button("Run script").apply {
            isDefaultButton = true
            minWidth = 105.0
            styleClass += "run-button"
        }
        val supportButton = Button("Need help? Come into WrScripts Discord").apply {
            graphic = loadSupportLogo()
            graphicTextGap = 8.0
            styleClass += "support-button"
            setOnAction { openSupportLink() }
        }
        val footer = HBox(
            8.0,
            supportButton,
            Region().apply { HBox.setHgrow(this, Priority.ALWAYS) },
            exitButton,
            runButton,
        ).apply {
            alignment = Pos.CENTER_RIGHT
            styleClass += "footer"
        }

        val root = BorderPane().apply {
            center = content
            bottom = footer
            styleClass += "app-root"
        }
        val scene = Scene(root, 520.0, 330.0)
        PumperGui::class.java.getResource("/org/tribot/wrblastpumper/gui/dark-theme.css")
            ?.toExternalForm()
            ?.let(scene.stylesheets::add)
        stage.scene = scene
        stage.minWidth = 540.0
        stage.minHeight = 320.0

        fun settingsFromForm(): PumperSettings = PumperSettings(
            world = worldField.text.trim().toIntOrNull(),
            refuel = refuelCheck.isSelected,
            stopAt = stoppingLevelField.text.trim().toInt(),
        )

        fun applySettings(settings: PumperSettings) {
            worldField.text = settings.world?.toString().orEmpty()
            refuelCheck.isSelected = settings.refuel
            stoppingLevelField.text = settings.stopAt.toString()
        }

        loadButton.setOnAction {
            profileStore.load(profileField.text)?.let(::applySettings)
        }

        saveButton.setOnAction {
            profileStore.save(profileField.text, settingsFromForm())
        }

        runButton.setOnAction {
            val settings = settingsFromForm()
            profileStore.save(PumperProfileStore.LAST_RUN_PROFILE, settings)
            stage.close()
            onComplete(settings)
        }

        exitButton.setOnAction {
            stage.close()
            onComplete(null)
        }

        stage.setOnCloseRequest {
            onComplete(null)
        }
    }

    private fun loadSupportLogo(): ImageView? = try {
        WrScriptHud::class.java.getResourceAsStream(SUPPORT_LOGO)?.use { stream ->
            ImageView(Image(stream)).apply {
                fitWidth = 24.0
                fitHeight = 24.0
                isPreserveRatio = true
                isSmooth = true
            }
        }
    } catch (e: Exception) {
        NullableLib.ctx.logger.warn("Could not load WrScripts logo", e)
        null
    }

    private fun openSupportLink() {
        try {
            if (
                !Desktop.isDesktopSupported() ||
                !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
            ) {
                NullableLib.ctx.logger.warn("Could not open Discord. Visit $SUPPORT_URL")
                return
            }

            Desktop.getDesktop().browse(URI(SUPPORT_URL))
        } catch (e: Exception) {
            NullableLib.ctx.logger.warn("Could not open Discord. Visit $SUPPORT_URL", e)
        }
    }

    private companion object {
        const val SUPPORT_URL = "https://discord.gg/Ju64CcbykJ"
        const val SUPPORT_LOGO = "/wrecked-avatar.png"
    }
}
