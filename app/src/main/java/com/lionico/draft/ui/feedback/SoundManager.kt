package com.lionico.draft.ui.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.lionico.draft.R
import com.lionico.draft.data.datastore.PlayerPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

enum class SoundType { MOVE, CAPTURE, PROMOTE, WIN, LOSE, DRAW }

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: PlayerPreferences
) {
    private val soundPool: SoundPool
    private val soundIds = mutableMapOf<SoundType, Int>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()

        soundIds[SoundType.MOVE]    = soundPool.load(context, R.raw.move, 1)
        soundIds[SoundType.CAPTURE] = soundPool.load(context, R.raw.capture, 1)
        soundIds[SoundType.PROMOTE] = soundPool.load(context, R.raw.promote, 1)
        soundIds[SoundType.WIN]     = soundPool.load(context, R.raw.win, 1)
        soundIds[SoundType.LOSE]    = soundPool.load(context, R.raw.lose, 1)
        soundIds[SoundType.DRAW]    = soundPool.load(context, R.raw.draw, 1)
    }

    private suspend fun isEnabled(): Boolean = preferences.soundEnabled.first()

    suspend fun play(type: SoundType) {
        if (!isEnabled()) return
        val id = soundIds[type] ?: return
        soundPool.play(id, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    fun release() {
        soundPool.release()
    }
}