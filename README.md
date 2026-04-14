Here's a comprehensive README.md file for the African Draughts app:

```markdown
# African Draughts

A classic two-player strategy board game built for Android using modern development practices.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Comose-1.7.0-green.svg)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange.svg)](https://developer.android.com/studio)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📱 About the Game

African Draughts (also known as Checkers or Draughts) is played on an 8×8 board using only the dark squares. Each player starts with 12 pieces, and the goal is to capture or block all of the opponent's pieces.

### Game Rules

- **Movement**: Men move forward diagonally one square at a time
- **Capturing**: Jump over opponent's pieces diagonally to capture them
- **Compulsory Captures**: If a capture is available, it must be taken
- **Multiple Captures**: Continue capturing with the same piece if possible
- **King Promotion**: Pieces reaching the far side become kings, able to move in all diagonal directions
- **Win Condition**: Capture all opponent's pieces or block them from moving
- **Draw**: Declared when neither player can force a win

### Features

- 🎮 **Two Game Modes**
  - Player vs Player (local multiplayer on same device)
  - Player vs Computer (AI opponent with multiple difficulties)

- 🤖 **Intelligent AI Opponent**
  - Three difficulty levels: Easy, Medium, Hard
  - Negamax algorithm with alpha-beta pruning
  - Optimized for smooth mobile performance

- ✨ **Modern UI**
  - Built with Jetpack Compose
  - Material 3 design system
  - Smooth animations and responsive touch controls
  - Edge-to-edge display support

- 📱 **Mobile Optimized**
  - Splash screen API support
  - Adaptive layouts for all screen sizes
  - Low battery impact
  - Offline play

## 🏗️ Architecture

This project follows modern Android development best practices and Clean Architecture principles.

### Tech Stack

| Category | Technology |
|:---|:---|
| UI Framework | Jetpack Compose |
| Dependency Injection | Hilt |
| Navigation | Compose Navigation |
| Concurrency | Kotlin Coroutines & Flow |
| Build System | Gradle with Kotlin DSL |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |

### Project Structure

```

app/src/main/java/com/lionico/draft/
├── data/                      # Data layer
│   ├── engine/                # Core game logic
│   │   ├── Board.kt           # Board representation (32-bit efficient)
│   │   ├── MoveValidator.kt   # Move validation and generation
│   │   ├── GameEngine.kt      # Game state management
│   │   └── Rules.kt           # Game rules constants
│   ├── ai/                    # AI opponent
│   │   ├── AIPlayer.kt        # Negamax implementation
│   │   ├── Evaluation.kt      # Board position scoring
│   │   └── Difficulty.kt      # Difficulty levels
│   └── model/                 # Data models
│       ├── Piece.kt
│       ├── Position.kt
│       ├── Move.kt
│       └── Player.kt
│
├── domain/                    # Domain layer
│   └── usecase/               # Business logic use cases
│       ├── ValidateMoveUseCase.kt
│       ├── ExecuteMoveUseCase.kt
│       ├── CheckGameOverUseCase.kt
│       └── GetAIMoveUseCase.kt
│
├── ui/                        # Presentation layer
│   ├── theme/                 # Material 3 theming
│   ├── screen/                # Full screen composables
│   │   ├── MainMenuScreen.kt
│   │   ├── GameScreen.kt
│   │   └── GameOverDialog.kt
│   ├── component/             # Reusable UI components
│   │   ├── BoardView.kt
│   │   ├── PieceView.kt
│   │   ├── GameStatusBar.kt
│   │   └── GameControls.kt
│   └── viewmodel/             # UI state management
│       └── GameViewModel.kt
│
└── di/                        # Dependency injection
└── AppModule.kt

```

### Design Patterns

- **MVVM**: Model-View-ViewModel for UI state management
- **Clean Architecture**: Separation of concerns across data, domain, and UI layers
- **Repository Pattern**: Abstracted data access (prepared for future persistence)
- **Use Case Pattern**: Single-responsibility business logic components
- **Dependency Injection**: Hilt for managing dependencies
- **Unidirectional Data Flow**: State flows from ViewModel to UI

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or newer
- Android SDK 35
- Gradle 8.5+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/african-draughts.git
   cd african-draughts
```

1. Open in Android Studio
   · Select "Open an Existing Project"
   · Navigate to the project directory and select it
   · Wait for Gradle sync to complete
2. Build and Run
   · Connect an Android device or start an emulator
   · Click the "Run" button (▶️) in Android Studio
   · Or use the command line:
     ```bash
     ./gradlew installDebug
     ```

Configuration

The app uses the template's pre-configured settings. Key configuration files:

· app/build.gradle.kts - App module configuration
· gradle/libs.versions.toml - Centralized dependency management
· app/src/main/AndroidManifest.xml - App manifest

🎮 How to Play

Main Menu

· Select "Player vs Player" for local two-player games
· Select "Player vs Computer" to play against the AI

During Gameplay

1. Select a Piece: Tap on one of your pieces (bottom for Player 1, top for Player 2/AI)
2. View Valid Moves: Highlighted squares show where you can move
3. Make a Move: Tap a highlighted square to move your piece
4. Capture: If a capture is available, you must take it
5. Multi-Capture: Continue capturing with the same piece until no more captures are available
6. King Promotion: Pieces reaching the opponent's back row become kings

Controls

· New Game: Reset the current game
· Back: Return to main menu (game progress will be lost)

🧠 AI Implementation

The computer opponent uses a Negamax algorithm with alpha-beta pruning for efficient search.

Difficulty Levels

Level Search Depth Description
Easy 2 Evaluates ~50 positions, plays quickly with basic strategy
Medium 4 Evaluates ~4,000 positions, solid intermediate play
Hard 6 Evaluates ~15,000 positions, challenging tactical play

Evaluation Function

The AI scores board positions based on:

· Material: Piece count (men = 100, kings = 175)
· Position: Center control bonus
· Mobility: Number of available moves

Performance Optimizations

· Alpha-Beta Pruning: Reduces search space by ~70%
· Background Processing: AI runs on coroutine dispatchers, never blocking UI
· Efficient Board Representation: 32-element IntArray for fast operations
· Incremental Updates: Board evaluation updates only changed positions

🧪 Testing

Unit Tests

Run unit tests with:

```bash
./gradlew test
```

Instrumentation Tests

Run instrumentation tests on a connected device:

```bash
./gradlew connectedAndroidTest
```

📦 Building for Release

Generate Signed APK/Bundle

1. Create a keystore (if you don't have one):
   ```bash
   keytool -genkey -v -keystore draft.keystore -alias draft -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Configure signing in app/build.gradle.kts:
   ```kotlin
   android {
       signingConfigs {
           create("release") {
               storeFile = file("draft.keystore")
               storePassword = System.getenv("KEYSTORE_PASSWORD")
               keyAlias = "draft"
               keyPassword = System.getenv("KEY_PASSWORD")
           }
       }
       buildTypes {
           release {
               signingConfig = signingConfigs.getByName("release")
           }
       }
   }
   ```
3. Build release APK:
   ```bash
   ./gradlew assembleRelease
   ```
4. Build App Bundle (recommended for Play Store):
   ```bash
   ./gradlew bundleRelease
   ```

🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (git checkout -b feature/amazing-feature)
3. Commit your changes (git commit -m 'Add amazing feature')
4. Push to the branch (git push origin feature/amazing-feature)
5. Open a Pull Request

Code Style

· Follow Kotlin coding conventions
· Use meaningful variable and function names
· Add KDoc comments for public APIs
· Keep composables small and focused
· Write unit tests for business logic

📝 Future Enhancements

Planned features for future releases:

· Game history and undo/redo support
· Save game state (DataStore/Room persistence)
· Sound effects and haptic feedback
· Online multiplayer (Firebase/WebRTC)
· Tournament mode with ELO ratings
· Additional draughts variants (International, Brazilian, Russian)
· Game replay and analysis
· Achievements and statistics
· Dark/Light theme toggle
· Accessibility improvements

📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

🙏 Acknowledgments

· Game Design: Traditional African Draughts rules
· AI Algorithm: Based on classic minimax/negamax with alpha-beta pruning
· UI Framework: Jetpack Compose
· Icons: Material Design Icons
· Template: Based on Lionico Android Template

📞 Contact

For questions, suggestions, or issues:

· Open an issue on GitHub
· Email: bchiyanike@gmail.com

---

Built with ❤️ using Kotlin and Jetpack Compose

```
