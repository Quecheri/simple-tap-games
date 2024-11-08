package polsl.game.server.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import polsl.game.R
import no.nordicsemi.android.common.theme.NordicTheme

@Composable
fun WaitingForClientsView() {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        Text(
            text = stringResource(id = R.string.looking_for_clients),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun WaitingForClientsView_Preview(){
    NordicTheme {
        WaitingForClientsView()
    }
}