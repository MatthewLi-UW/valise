package com.valise.mobile

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.valise.mobile.controller.LoginController
import com.valise.mobile.controller.OverviewController
import com.valise.mobile.model.Model
import com.valise.mobile.ui.theme.MobileTheme
import com.valise.mobile.view.LandingPage
import com.valise.mobile.view.OverviewList
import com.valise.mobile.view.OverviewViewModel
import com.valise.mobile.view.TripAddColumn
import com.valise.mobile.view.TripSummaryList
import com.valise.mobile.view.LoginViewModel
import com.google.android.libraries.places.api.Places
import com.valise.mobile.BuildConfig.PLACES_API_KEY
import com.valise.mobile.controller.MapController
import com.valise.mobile.controller.SummaryController
import com.valise.mobile.view.MapView
import com.valise.mobile.view.MapViewModel
import com.valise.mobile.view.SummaryViewModel
import java.util.UUID
import com.valise.mobile.view.TripCitiesList

// jesus christ please use this instead of the google compose documentation
// https://composables.com/

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


        val model = Model()

        setContent {
            MobileTheme {
                RootNavHost(model)
            }
        }

        // from google documentation
        // Define a variable to hold the Places API key.
        // I CANT FIGURE OUT HOW TO DO THIS SAFELY ????????????????
        // val apiKey = BuildConfig.PLACES_API_KEY
        // i guess my api key is just here for now dont hack me
        val apiKey = PLACES_API_KEY

        // Log an error if apiKey is not set.
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e("Places test", "No api key")
            finish()
            return
        }

        // Initialize the SDK
//        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        Places.initialize(applicationContext, apiKey)
        // Create a new PlacesClient instance
//        val placesClient = Places.createClient(this)

    }
}

@Composable
fun RootNavHost(model : Model) {
    val navController = rememberNavController()
    val overviewViewModel = OverviewViewModel(model)
    val overviewController = OverviewController(model)
    val summaryController = SummaryController(model)
    val summaryViewModel = SummaryViewModel(model)

    val loginViewModel = LoginViewModel(model)
    val loginController = LoginController(model)
    val mapViewModel = MapViewModel(model)
    val mapController = MapController(model)

    NavHost(
        navController = navController,
        startDestination = Login
    ) {
        composable<Login> {
            LandingPage(loginViewModel, loginController, navController)
        }
        composable<Home> {
            OverviewList(overviewViewModel, overviewController, navController)
        }
        composable<TripAdd> {
            TripAddColumn(overviewViewModel, overviewController, navController)
        }
        composable<TripSummary> {
            val args = it.toRoute<TripSummary>()
            TripSummaryList(model.list.first{it.tripId == UUID.fromString(args.tripIdAsString)},
                summaryViewModel, summaryController, navController)
        }
        composable<UpdateTrip> {
            val args = it.toRoute<UpdateTrip>()
            TripCitiesList(overviewViewModel.list[args.tripIndex], 0, overviewViewModel, overviewController, navController)
        }

        composable<MapView> {
            val args = it.toRoute<MapView>()
            val tripId = UUID.fromString(args.tripIdAsString)
            val selectedEventId = args.selectedEventIdAsString?.let { UUID.fromString(it) }
            MapView(
                tripId = tripId,
                mapViewModel = mapViewModel,
                mapController = mapController,
                navController = navController,
                selectedEventId = selectedEventId
            )
        }
    }
}

@Preview
@Composable
fun TaskListPreview() {
    val model = Model()
    val viewModel = OverviewViewModel(model)
    val controller = OverviewController(model)

//    OverviewList(viewModel, controller)
}

//https://proandroiddev.com/remove-ripple-effect-from-clickable-and-toggleable-widget-in-jetpack-compose-16b154265283
inline fun Modifier.noRippleClickable(
    crossinline onClick: () -> Unit
): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}
