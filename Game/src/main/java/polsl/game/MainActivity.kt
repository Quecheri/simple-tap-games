package polsl.game

import android.icu.text.IDNA.Info
import android.os.Bundle
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import polsl.game.navigation.ClientDestinations
import polsl.game.navigation.ServerDestinations
import polsl.game.navigation.StartScreenDestination
import no.nordicsemi.android.common.navigation.NavigationView
import no.nordicsemi.android.common.theme.NordicActivity
import no.nordicsemi.android.common.theme.NordicTheme
import polsl.game.navigation.InfoDestinations

@AndroidEntryPoint
class MainActivity : NordicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NordicTheme {
                NavigationView(
                    destinations = StartScreenDestination
                            + ServerDestinations
                            + ClientDestinations
                            + InfoDestinations
                )
            }
        }
    }
}