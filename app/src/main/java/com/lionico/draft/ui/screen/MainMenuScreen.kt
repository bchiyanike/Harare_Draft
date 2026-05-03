// File: app/src/main/java/com/lionico/draft/ui/screen/MainMenuScreen.kt
package com.lionico.draft.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lionico.draft.R
import com.lionico.draft.data.ai.Difficulty
import com.lionico.draft.data.model.TimeControl
import com.lionico.draft.ui.component.ClockSelectionSheet
import com.lionico.draft.ui.component.FriendOptionsSheet
import com.lionico.draft.ui.component.PlayerNameDialog
import com.lionico.draft.ui.component.SectionHeader
import com.lionico.draft.ui.theme.live_badge_red
import com.lionico.draft.ui.viewmodel.MainMenuViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onPlayVsFriend: (TimeControl) -> Unit,
    onPlayVsComputer: (TimeControl) -> Unit,
    onHistory: () -> Unit = {},
    onSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MainMenuViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showNameDialog by remember { mutableStateOf(false) }
    var showClockSheet by remember { mutableStateOf(false) }
    var selectedClock by remember { mutableStateOf<TimeControl?>(null) }
    var pendingMode by remember { mutableStateOf<String?>(null) }
    var showFriendOptions by remember { mutableStateOf(false) }

    val playerNames by viewModel.playerNames.collectAsStateWithLifecycle(initialValue = null)
    val currentDifficulty by viewModel.difficulty.collectAsStateWithLifecycle(initialValue = Difficulty.MEDIUM)
    val playerRating by viewModel.playerRating.collectAsStateWithLifecycle(initialValue = 1200)

    val showComingSoonToast: () -> Unit = {
        Toast.makeText(context, R.string.coming_soon, Toast.LENGTH_SHORT).show()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.castle_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with rating badge and settings
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${playerRating.roundToInt()} Elo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = onSettings,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = Color.White,
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }

            item {
                SectionHeader(title = stringResource(R.string.quick_play))
            }
            item {
                Button(
                    onClick = {
                        pendingMode = "friend"
                        showClockSheet = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.play_with_friend_button),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        pendingMode = "computer"
                        showClockSheet = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.play_with_computer_button),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                SectionHeader(title = stringResource(R.string.live_now))
            }

            item {
                CompactLiveCard(
                    title = stringResource(R.string.app_name),
                    description = stringResource(R.string.tagline),
                    badge = stringResource(R.string.live_now)
                )
            }

            item {
                Button(
                    onClick = showComingSoonToast,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.create_game),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Button(
                    onClick = onHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.game_history_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (showClockSheet) {
            ClockSelectionSheet(
                onSelect = { clock ->
                    showClockSheet = false
                    selectedClock = clock
                    if (pendingMode == "friend") {
                        showFriendOptions = true
                    } else if (pendingMode == "computer") {
                        onPlayVsComputer(clock)
                    }
                    pendingMode = null
                },
                onDismiss = {
                    showClockSheet = false
                    pendingMode = null
                }
            )
        }

        if (showFriendOptions && selectedClock != null) {
            FriendOptionsSheet(
                onSameDevice = {
                    showFriendOptions = false
                    onPlayVsFriend(selectedClock!!)
                },
                onBluetooth = {
                    showFriendOptions = false
                    showComingSoonToast()
                },
                onOnline = {
                    showFriendOptions = false
                    showComingSoonToast()
                },
                onDismiss = {
                    showFriendOptions = false
                }
            )
        }

        if (showNameDialog && playerNames != null) {
            PlayerNameDialog(
                currentPlayer1Name = playerNames!!.player1Name,
                currentPlayer2Name = playerNames!!.player2Name,
                currentDifficulty = currentDifficulty,
                isPlayerVsAI = false,
                onConfirm = { name1, name2, difficulty ->
                    scope.launch {
                        viewModel.setPlayerNames(name1, name2)
                        viewModel.setDifficulty(difficulty)
                    }
                    showNameDialog = false
                },
                onDismiss = { showNameDialog = false }
            )
        }
    }
}

@Composable
private fun CompactLiveCard(
    title: String,
    description: String,
    badge: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
            Text(
                text = badge,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = live_badge_red,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}