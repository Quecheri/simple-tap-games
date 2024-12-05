package polsl.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
            title = { Text("Informacje") },
            onNavigationButtonClick = onNavigationUp
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeaderText(text = "Nim")
            }
            item {
                AnotatedText(
                    text = buildAnnotatedString {
                        append("Liczba graczy równa liczbie telefonów. Na ekranie wyświetlana jest liczba pozostałych ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("zapałek") }
                        append(". Celem gry jest zabieranie zapałek w taki sposób, aby nie zostać z ostatnią. ")
                        append("W swojej turze gracz może zabrać od ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("1 do 3 zapałek") }
                        append(", naciskając odpowiedni przycisk. Gra trwa do momentu, gdy zostanie zabrana ostatnia zapałka. ")
                        append("Gracz, który ją weźmie, przegrywa. Jeśli gracz nie wybierze liczby zapałek w określonym czasie, ")
                        append("automatycznie traci swoją turę, zabierając jedną zapałkę.")
                    }
                )
            }
            item {
                HeaderText(text = "Szybkość reakcji")
            }
            item {
                AnotatedText(
                    text = buildAnnotatedString {
                        append("Gra dla jednego gracza. Na jednym z podłączonych urządzeń wyświetlana jest grafika ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("kapibary") }
                        append(" lub ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("bobra") }
                        append(". Celem gracza jest jak najszybsze ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("kliknięcie") }
                        append(" w ekran z ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("bobrem") }
                        append(" albo ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("unikanie kliknięcia")}
                        append(" w ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("kapibarę")}
                        append(". Jeśli gracz nie naciśnie żadnego ekranu w określonym czasie, ")
                        append("będzie to uznane za błędną reakcję w przypadku bobra i prawidłową w przypadku kapibary. ")
                        append("Po zakończeniu gry wyświetlane jest podsumowanie czasów reakcji.")
                    }
                )
            }
            item {
                TwoImagesInRow()
            }
            item {
                HeaderText(text = "Kombinacje")
            }
            item {
                AnotatedText(
                    text = buildAnnotatedString {
                        append("Gra dla jednego gracza. Na początku za pomocą ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("żółtych błysków") }
                        append(" wyświetlana jest kombinacja. Gracz musi zapamiętać kolejność błyskających urządzeń, ")
                        append("a następnie odtworzyć ją, klikając w prawidłowe urządzenia w odpowiedniej kolejności. ")
                        append("Naciśnięcie prawidłowego urządzenia sygnalizowane jest ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("zielonym błyskiem") }
                        append(", a nieprawidłowego – ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("czerwonym") }
                        append(". Po każdej udanej rundzie kombinacja wydłuża się o jeden element. Gra kończy się po błędnym kliknięciu ")
                        append("lub ukończeniu wszystkich rund, a następnie wyświetlane jest podsumowanie.")
                    }
                )
            }
            item {
                HeaderText(text = "Licencje")
            }
            item {
                AnotatedText(
                    textAlign = TextAlign.Left,
                    text = buildAnnotatedString {
                        append("Ten projekt korzysta z następujących bibliotek open source: \n")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Biblioteka Nordic Semiconductor\n") }
                        withStyle(style = SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Light)) { append(" (Copyright (c) 2015, Nordic Semiconductor)\n") }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Licencja") }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) { append(" BSD 3-Clause License\n") }
                        append("Szczegóły znajdują się w pliku LICENSE")

                    }
                )
            }
            item {
                HeaderText(text = "Autorzy")
            }
            item {
                StandardText(text = "Bartłomiej Piątek\nJakub Hoś")
                StandardText(text = " ")
                StandardText(text = " ")
            }
        }
    }
}

@Composable
fun HeaderText(
    text: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

@Composable
fun AnotatedText(
    text: AnnotatedString,
    textAlign: TextAlign = TextAlign.Justify,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        textAlign = textAlign

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

@Composable
fun TwoImagesInRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally)
        {
            Image(
                painter = painterResource(id = polsl.game.R.drawable.beaver),
                contentDescription = "beaver",
                modifier = Modifier
                    .size(150.dp)
            )
            Text(
                text = "Bóbr",
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally)
        {
            Image(
                painter = painterResource(id = polsl.game.R.drawable.capybara),
                contentDescription = "capybara",
                modifier = Modifier
                    .size(150.dp)
            )
            Text(
                text = "Kapibara",
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
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
