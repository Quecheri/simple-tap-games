package polsl.game.server.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import polsl.game.R
import polsl.game.server.data.Player
import no.nordicsemi.android.common.theme.NordicTheme
import polsl.game.server.viewmodel.GameType

@Composable
fun StartGameView(
    isAllNameCollected: Boolean,
    joinedPlayer: List<Player>,
    onStartGame: (GameType, String, String) -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.players),
                modifier = Modifier.padding(8.dp)
            )

            joinedPlayer.forEach { player ->
                Text(
                    text = player.name,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Text(
                text = stringResource(id = R.string.select_game),
                modifier = Modifier.padding(8.dp)
            )

            var selectedGame by remember { mutableStateOf(GameType.NIM) }
            var timeForReaction by remember { mutableStateOf("") }
            var numberOfRounds by remember { mutableStateOf("") }

            GameType.entries.forEach { game ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedGame = game }
                ) {
                    RadioButton(
                        selected = (game == selectedGame),
                        onClick = { selectedGame = game }
                    )
                    Text(
                        text = game.toString(),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = timeForReaction,
                onValueChange = { timeForReaction = it },
                label = { Text(stringResource(R.string.reaction_time)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = numberOfRounds,
                onValueChange = { numberOfRounds = it },
                label = { Text(stringResource(R.string.round_number)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                onClick = { onStartGame(selectedGame, timeForReaction, numberOfRounds) },
                enabled = isAllNameCollected,
            ) {
                Text(text = stringResource(id = R.string.start_game))
            }
        }
    }
}


@Preview
@Composable
fun StartGameView_Preview() {
    NordicTheme {
        StartGameView(
            isAllNameCollected = false,
            joinedPlayer = listOf(
                Player("User 1"),
                Player("User 2")
            )
        ) { game,timeout,rounds-> }
    }
}