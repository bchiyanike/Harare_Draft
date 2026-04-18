package com.lionico.draft

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LionicoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupCrashHandler()
        // existing Hilt or other setups can go here
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val file = java.io.File(filesDir, "crash_log.txt")
                file.writeText(
                    buildString {
                        appendLine("=== CRASH @ ${java.util.Date()} ===")
                        appendLine("Thread: ${thread.name}")
                        appendLine(throwable.stackTraceToString())
                    }
                )
            } catch (_: Exception) {
                // don't let crash handler crash
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}