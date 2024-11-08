package polsl.game.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import polsl.game.server.ServerScreen
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel

val ServerDestination = createSimpleDestination(NavigationConst.SERVER)
private val Server = defineDestination(ServerDestination) {
    val viewModel: SimpleNavigationViewModel = hiltViewModel()

    ServerScreen(
        onNavigationUp = { viewModel.navigateUp() }
    )
}
val ServerDestinations = Server