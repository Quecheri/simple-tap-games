package polsl.game.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.common.ui.view.NordicAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    onNavigationUp: () -> Unit,
) {
    Column {
        NordicAppBar(
            title = { Text("Instrukcja") },
            onNavigationButtonClick = onNavigationUp
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                HeaderText(text = "Działanie aplikacji")
            }
            item {
                StandardText(text = "tu działanie")
            }
        }
    }
}

@Composable
fun HeaderText(
    text: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun StandardText(
    text: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
) {
    Text(
        text = text,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun InfoScreen_Preview() {
    NordicTheme {
        InfoScreen(
            onNavigationUp = {}
        )
    }
}
