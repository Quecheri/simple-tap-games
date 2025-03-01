package polsl.game.client.view

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

@Composable
fun InitializingView() {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        Text(
            text = stringResource(id = R.string.initializing),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
fun InitializingView_Preview() {
    InitializingView()
}