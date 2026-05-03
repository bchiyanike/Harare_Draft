// File: app/src/main/java/com/lionico/draft/domain/usecase/UpdateRatingUseCase.kt
package com.lionico.draft.domain.usecase

import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Calculates new Elo ratings after a game.
 *
 * [K_FACTOR] controls how quickly ratings adjust. 32 is a common value
 * for amateur/club play and keeps climbs/losses noticeable but not volatile.
 */
class UpdateRatingUseCase @Inject constructor() {

    companion object {
        const val K_FACTOR = 32f
        const val SCALE = 400f
    }

    /**
     * Returns a pair (newRating, ratingDelta) for a given outcome.
     *
     * @param playerRating   Current rating of the player.
     * @param opponentRating Current rating of the opponent (static for AI).
     * @param score          Outcome: 1.0 = win, 0.0 = loss, 0.5 = draw.
     */
    operator fun invoke(
        playerRating: Float,
        opponentRating: Float,
        score: Float
    ): Pair<Float, Int> {
        val expected = 1.0f / (1.0f + Math.pow(
            10.0,
            ((opponentRating - playerRating) / SCALE).toDouble()
        ).toFloat())
        val delta = (K_FACTOR * (score - expected)).roundToInt()
        val newRating = (playerRating + delta).coerceAtLeast(0f)
        return newRating to delta
    }
}