package polsl.game.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import polsl.game.R
import no.nordicsemi.android.common.ui.view.NordicAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    onServerNavigation: () -> Unit,
    onClientNavigation: () -> Unit,
    onInfoNavigation: () -> Unit,
) {
    Column {
        NordicAppBar(
            title = { Text(text = stringResource(id = R.string.welcome_message)) },
            actions = {
                IconButton(
                    onClick = { onInfoNavigation() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.start_game_description)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(
                    onClick = onServerNavigation
                ) {
                    Text(text = stringResource(id = R.string.start_game))
                }
                Button(
                    onClick = onClientNavigation
                ) {
                    Text(text = stringResource(id = R.string.join_game))
                }
            }
        }
    }
}