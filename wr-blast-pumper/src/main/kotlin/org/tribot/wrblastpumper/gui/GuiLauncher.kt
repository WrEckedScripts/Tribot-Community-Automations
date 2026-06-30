package org.tribot.wrblastpumper.gui

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.stage.Modality
import javafx.stage.Stage
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.swing.SwingUtilities

object GuiLauncher {
    fun create(title: String, build: (Stage) -> Unit): Stage? {
        val toolkitReady = CountDownLatch(1)
        val stageReady = CountDownLatch(1)
        val stageReference = AtomicReference<Stage?>()

        SwingUtilities.invokeLater {
            runCatching {
                JFXPanel()
                Platform.setImplicitExit(false)
            }
            toolkitReady.countDown()
        }

        if (!await(toolkitReady, 5)) return null

        val stageScheduled = runCatching {
            Platform.runLater {
                try {
                    val stage = Stage().apply {
                        this.title = title
                        initModality(Modality.APPLICATION_MODAL)
                        isResizable = true
                    }
                    stageReference.set(stage)
                    build(stage)
                } catch (_: Throwable) {
                    stageReference.set(null)
                } finally {
                    stageReady.countDown()
                }
            }
        }.isSuccess

        if (!stageScheduled) return null

        return if (await(stageReady, 10)) stageReference.get() else null
    }

    private fun await(latch: CountDownLatch, seconds: Long): Boolean = try {
        latch.await(seconds, TimeUnit.SECONDS)
    } catch (_: InterruptedException) {
        Thread.currentThread().interrupt()
        false
    }
}
