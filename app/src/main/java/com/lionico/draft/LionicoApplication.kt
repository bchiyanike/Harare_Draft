// File: app/src/main/java/com/lionico/draft/LionicoApplication.kt
package com.lionico.draft

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.util.Date

@HiltAndroidApp
class LionicoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupCrashHandler()
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                File(filesDir, "crash_log.txt").writeText(
                    buildString {
                        appendLine("=== CRASH @ ${Date()} ===")
                        appendLine("Thread: ${thread.name}")
                        appendLine(throwable.stackTraceToString())
                    }
                )
            } catch (_: Exception) {}

            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
