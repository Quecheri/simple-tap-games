package polsl.game.client.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import polsl.game.R

@Composable
fun DisconnectedView(reason: ConnectionState.Disconnected.Reason) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.disconnected),
            )
            Text(
                text = reason.name(),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview
@Composable
fun DisconnectedView_Preview(){
    DisconnectedView(ConnectionState.Disconnected.Reason.TERMINATE_LOCAL_HOST)
}

private fun ConnectionState.Disconnected.Reason.name(): String = when (this) {
    ConnectionState.Disconnected.Reason.TERMINATE_PEER_USER -> "Rozłączono przez klienta"
    ConnectionState.Disconnected.Reason.TERMINATE_LOCAL_HOST -> "Rozłączono przez hosta"
    ConnectionState.Disconnected.Reason.CANCELLED -> "Anulowano"
    ConnectionState.Disconnected.Reason.LINK_LOSS -> "Połączenie utracone"
    ConnectionState.Disconnected.Reason.NOT_SUPPORTED -> "Brak wsparcia"
    ConnectionState.Disconnected.Reason.TIMEOUT -> "Przekroczono limit czasu oczekiwania na odpowiedź"
    ConnectionState.Disconnected.Reason.UNKNOWN -> "Nieznany błąd"
    else -> "Nieznany błąd"
}