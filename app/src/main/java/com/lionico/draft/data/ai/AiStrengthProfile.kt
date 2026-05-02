// File: app/src/main/java/com/lionico/draft/data/ai/AiStrengthProfile.kt
package com.lionico.draft.data.ai

/**
 * Defines the AI's playing strength for a given Elo rating.
 *
 * @property eloRating      Fixed rating of the AI opponent (900–2700).
 * @property maxDepth       Maximum search depth for the negamax algorithm.
 * @property mistakeProbability Chance (0.0–1.0) that the AI will pick a random legal move
 *                              instead of its best computed move, simulating human error.
 */
data class AiStrengthProfile(
    val eloRating: Int,
    val maxDepth: Int,
    val mistakeProbability: Float
) {
    companion object {
        /** Nine predefined strength levels from beginner to world‑class. */
        val PRESETS: List<AiStrengthProfile> = listOf(
            AiStrengthProfile(eloRating = 900,  maxDepth = 1,  mistakeProbability = 0.40f),
            AiStrengthProfile(eloRating = 1125, maxDepth = 2,  mistakeProbability = 0.30f),
            AiStrengthProfile(eloRating = 1350, maxDepth = 3,  mistakeProbability = 0.20f),
            AiStrengthProfile(eloRating = 1575, maxDepth = 4,  mistakeProbability = 0.12f),
            AiStrengthProfile(eloRating = 1800, maxDepth = 6,  mistakeProbability = 0.05f),
            AiStrengthProfile(eloRating = 2025, maxDepth = 8,  mistakeProbability = 0.02f),
            AiStrengthProfile(eloRating = 2250, maxDepth = 10, mistakeProbability = 0.01f),
            AiStrengthProfile(eloRating = 2475, maxDepth = 12, mistakeProbability = 0.005f),
            AiStrengthProfile(eloRating = 2700, maxDepth = 12, mistakeProbability = 0.001f)
        )

        /** Default profile used when none is selected (casual strength). */
        val DEFAULT = PRESETS[3] // 1575

        /** Returns the profile for a given Elo rating, or DEFAULT if not found. */
        fun forRating(rating: Int): AiStrengthProfile =
            PRESETS.firstOrNull { it.eloRating == rating } ?: DEFAULT
    }
}