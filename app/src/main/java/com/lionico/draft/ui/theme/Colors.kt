// File: app/src/main/java/com/lionico/draft/ui/theme/Colors.kt
package com.lionico.draft.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Game-specific colors
val DarkSquareColor = Color(0xFF8B4513)      // Brown for dark squares
val LightSquareColor = Color(0xFFF5DEB3)     // Wheat/beige for light squares
val Player1PieceColor = Color(0xFFD32F2F)    // Red for bottom player
val Player2PieceColor = Color(0xFF1976D2)    // Blue for top player
val ValidMoveHighlight = Color(0xFF4CAF50).copy(alpha = 0.4f)  // Green with transparency
val SelectedPieceBorder = Color(0xFFFFD700)  // Gold for selected piece
val board_selected_square_highlight = Color(0xFFFFEB3B).copy(alpha = 0.3f) // yellow highlight for selected square

// Live badge
val live_badge_red = Color(0xFFFF3B30)       // red for LIVE badge

// Analysis arrows
val PlayedArrowColor = Color(0xFF00BCD4)     // Cyan for the move actually played
val BestArrowColor = Color(0xFF4CAF50)       // Green for AI's best suggested move

// Move list styling (used in ReplayScreen)
val MovePlayedColor = Color.White.copy(alpha = 0.4f)   // dimmed for played moves
val MoveUpcomingColor = Color.White.copy(alpha = 0.9f) // upcoming, nearly full white
val MoveCurrentBg = Color(0xFFFFD54F).copy(alpha = 0.3f) // soft yellow highlight
val MoveCurrentText = Color.White
val RedSideColor = Color(0xFFD32F2F)   // serious red
val BlackSideColor = Color(0xFF424242) // serious dark grey/black

// Rating delta colours
val RatingPositiveGreen = Color(0xFF2E7D32)   // Tournament Felt / positive change
val RatingNegativeRed = Color(0xFFC0392B)     // Crimson Tension / negative change
val RatingNeutralGray = Color(0xFF9E9E9E)     // Neutral / zero change

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFFD0BCFF),
    surfaceDim = Color(0xFFDED8E1),
    surfaceBright = Color(0xFFFFFBFE),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F2FA),
    surfaceContainer = Color(0xFFF3EDF7),
    surfaceContainerHigh = Color(0xFFECE6F0),
    surfaceContainerHighest = Color(0xFFE6E0E9),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = Color(0xFF6750A4),
    surfaceDim = Color(0xFF141218),
    surfaceBright = Color(0xFF3B383E),
    surfaceContainerLowest = Color(0xFF0F0D13),
    surfaceContainerLow = Color(0xFF1D1B20),
    surfaceContainer = Color(0xFF211F26),
    surfaceContainerHigh = Color(0xFF2B2930),
    surfaceContainerHighest = Color(0xFF36343B),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)