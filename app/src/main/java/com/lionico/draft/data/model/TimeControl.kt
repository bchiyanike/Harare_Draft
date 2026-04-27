// File: app/src/main/java/com/lionico/draft/data/model/TimeControl.kt
package com.lionico.draft.data.model

data class TimeControl(
    val baseMinutes: Int,
    val incrementSeconds: Int
) {
    val totalSeconds: Int get() = baseMinutes * 60

    /** Human-readable label, e.g., "3+2" or "5" */
    fun label(): String = if (incrementSeconds > 0) "$baseMinutes+$incrementSeconds" else "$baseMinutes"

    companion object {
        val PRESETS = listOf(
            TimeControl(1, 0),
            TimeControl(1, 1),
            TimeControl(2, 0),
            TimeControl(2, 1),
            TimeControl(3, 0),
            TimeControl(3, 2),
            TimeControl(5, 0)
        )
        val DEFAULT = TimeControl(3, 2)
    }
}