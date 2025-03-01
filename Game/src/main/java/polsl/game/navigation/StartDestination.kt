package polsl.game.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import polsl.game.view.StartScreen
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel

val StartDestination = createSimpleDestination(NavigationConst.START)
private val Start = defineDestination(StartDestination) {
    val viewModel: SimpleNavigationViewModel = hiltViewModel()

    StartScreen(
        onServerNavigation = {viewModel.navigateTo(ServerDestination)},
        onClientNavigation =  {viewModel.navigateTo(ClientDestination)},
        onInfoNavigation =  {viewModel.navigateTo(AdditionalInfoDestination)},
    )
}
val StartScreenDestination = Start