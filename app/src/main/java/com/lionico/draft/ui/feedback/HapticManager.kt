package com.lionico.draft.ui.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.lionico.draft.data.datastore.PlayerPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HapticManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: PlayerPreferences
) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(VibratorManager::class.java)
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private suspend fun isEnabled(): Boolean = preferences.hapticEnabled.first()

    private fun vibrate(effect: VibrationEffect) {
        vibrator.cancel()
        vibrator.vibrate(effect)
    }

    suspend fun selectPiece() {
        if (!isEnabled()) return
        vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    suspend fun movePiece() {
        if (!isEnabled()) return
        vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    suspend fun capture() {
        if (!isEnabled()) return
        val timings = longArrayOf(0, 20, 30, 20)    // wait, vibrate, pause, vibrate
        val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    }

    suspend fun invalidTap() {
        if (!isEnabled()) return
        vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}