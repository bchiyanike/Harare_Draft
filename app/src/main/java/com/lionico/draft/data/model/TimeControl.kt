// File: app/src/main/java/com/lionico/draft/data/model/TimeControl.kt
package com.lionico.draft.data.model

data class TimeControl(
    val baseMinutes: Double,
    val incrementSeconds: Int
) {
    val totalSeconds: Int get() = (baseMinutes * 60).toInt()

    /** Human-readable label, e.g. "½+0", "3+2", "5+0" */
    fun label(): String {
        val baseStr = if (baseMinutes == 0.5) "½" else baseMinutes.toInt().toString()
        return if (incrementSeconds > 0) "$baseStr+$incrementSeconds" else baseStr
    }

    fun categoryLabel(): String = when {
        baseMinutes <= 0.5 -> "Lightning"
        baseMinutes <= 1.0 -> "Bullet"
        baseMinutes <= 3.0 -> "Blitz"
        baseMinutes <= 5.0 -> "Rapid"
        else -> "Classical"
    }

    companion object {
        val PRESETS = listOf(
            TimeControl(0.5, 0),
            TimeControl(0.5, 1),
            TimeControl(1.0, 0),
            TimeControl(1.0, 1),
            TimeControl(2.0, 0),
            TimeControl(2.0, 1),
            TimeControl(3.0, 0),
            TimeControl(3.0, 2),
            TimeControl(5.0, 0)
        )
    }
}