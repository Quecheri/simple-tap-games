package polsl.game.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import polsl.game.client.ClientScreen
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel

val ClientDestination = createSimpleDestination(NavigationConst.CLIENT)
private val Client = defineDestination(ClientDestination) {
    val viewModel: SimpleNavigationViewModel = hiltViewModel()

    ClientScreen(
        onNavigationUp = { viewModel.navigateUp() }
    )
}
val ClientDestinations = Client