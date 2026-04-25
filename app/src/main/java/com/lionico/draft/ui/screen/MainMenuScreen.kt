// File: app/src/main/java/com/lionico/draft/ui/screen/MainMenuScreen.kt
package com.lionico.draft.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lionico.draft.R
import com.lionico.draft.data.ai.Difficulty
import com.lionico.draft.ui.component.PlayerNameCard
import com.lionico.draft.ui.component.PlayerNameDialog
import com.lionico.draft.ui.component.QuickPlayGrid
import com.lionico.draft.ui.component.SectionHeader
import com.lionico.draft.ui.component.StatBadge
import com.lionico.draft.ui.component.StreamCard
import com.lionico.draft.ui.component.FriendOptionsCard
import com.lionico.draft.ui.component.AIDifficultySheet
import com.lionico.draft.ui.component.StreamData
import com.lionico.draft.ui.component.sampleStreams
import com.lionico.draft.ui.viewmodel.MainMenuViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onPlayVsFriendSameDevice: () -> Unit,
    onPlayVsAI: (Difficulty) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainMenuViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showAIDifficultySheet by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var crashLog by remember { mutableStateOf("") }

    val playerNames by viewModel.playerNames.collectAsStateWithLifecycle(initialValue = null)

    val showComingSoonToast: () -> Unit = {
        Toast.makeText(context, R.string.coming_soon, Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    StatBadge(
                        icon = Icons.Default.Person,
                        count = "105.9K",
                        label = stringResource(R.string.players_label)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    StatBadge(
                        icon = Icons.Default.Group,
                        count = "44.2K",
                        label = stringResource(R.string.games_label)
                    )
                }
            }
        }

        item {
            PlayerNameCard(
                playerName = playerNames?.player1Name ?: stringResource(R.string.you),
                onEditClick = { showNameDialog = true }
            )
        }

        item {
            SectionHeader(title = stringResource(R.string.quick_play))
        }

        item {
            QuickPlayGrid(
                onPlayVsFriendSameDevice = onPlayVsFriendSameDevice,
                onPlayVsAI = { showAIDifficultySheet = true }
            )
        }

        item {
            SectionHeader(title = stringResource(R.string.play_with_friend))
        }

        item {
            FriendOptionsCard(
                onSameDevice = onPlayVsFriendSameDevice,
                onBluetooth = showComingSoonToast,
                onInternet = showComingSoonToast
            )
        }

        item {
            SectionHeader(title = stringResource(R.string.live_now))
        }

        items(sampleStreams) { stream ->
            StreamCard(stream, onClick = showComingSoonToast)
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

        // --- DEBUG: Crash log viewer — remove before release ---
        item {
            Button(
                onClick = {
                    val file = File(context.filesDir, "crash_log.txt")
                    crashLog = if (file.exists()) file.readText() else "No crash log found."
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Show Crash Log (Debug)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        // --- END DEBUG ---
    }

    // --- DEBUG: Crash log dialog — remove before release ---
    if (crashLog.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { crashLog = "" },
            confirmButton = {
                TextButton(onClick = { crashLog = "" }) {
                    Text("Close")
                }
            },
            title = { Text("Crash Log") },
            text = {
                Text(
                    text = crashLog,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }
        )
    }
    // --- END DEBUG ---

    if (showNameDialog && playerNames != null) {
        PlayerNameDialog(
            currentPlayer1Name = playerNames!!.player1Name,
            currentPlayer2Name = playerNames!!.player2Name,
            isPlayerVsAI = false,
            onConfirm = { name1, name2 ->
                scope.launch {
                    viewModel.setPlayerNames(name1, name2)
                }
                showNameDialog = false
            },
            onDismiss = { showNameDialog = false }
        )
    }

    if (showAIDifficultySheet) {
        AIDifficultySheet(
            onSelect = { difficulty ->
                showAIDifficultySheet = false
                onPlayVsAI(difficulty)
            },
            onDismiss = { showAIDifficultySheet = false }
        )
    }
}
