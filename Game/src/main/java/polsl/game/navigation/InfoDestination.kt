package polsl.game.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import polsl.game.view.InfoScreen


val AdditionalInfoDestination = createSimpleDestination(NavigationConst.INFO)
private val info = defineDestination(AdditionalInfoDestination) {
    val viewModel: SimpleNavigationViewModel = hiltViewModel()

    InfoScreen(
        onNavigationUp = { viewModel.navigateUp() }
    )
}
val InfoDestinations = info