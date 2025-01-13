package com.valise.mobile.view

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.valise.mobile.Home
import com.valise.mobile.R
import com.valise.mobile.controller.OverviewController
import com.valise.mobile.entities.City
import com.valise.mobile.entities.Trip
import com.valise.mobile.ui.theme.Transparent
import com.valise.mobile.ui.theme.ValiseDarkDarkGray
import com.valise.mobile.ui.theme.ValiseDarkGray
import com.valise.mobile.ui.theme.ValiseLightGray
import java.util.UUID

// types of actions that our controller understands
enum class UpdateTripControllerEvent { Add, Del, Update }

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun TripCitiesList(
    trip: Trip,
    tripIndex: Int,
    taskListViewModel: OverviewViewModel,
    taskListController: OverviewController,
    navController: NavController
) {
    val viewModel by remember { mutableStateOf(taskListViewModel) }
    val controller by remember { mutableStateOf(taskListController) }
    var tripName by remember { mutableStateOf(trip.destination ?: "") }
    var tripDesc by remember { mutableStateOf(trip.description ?: "") }
    var showAddCity by remember { mutableStateOf(false) }
    var tripCities by remember { mutableStateOf(trip.cities.toMutableList()) }
    val context = LocalContext.current
    var placeId by remember {mutableStateOf("")}
    var expandedCities by remember {
        mutableStateOf(
            tripCities.associate { it.cityId to false }.toMutableMap()
        )
    }
    val josefinsans = FontFamily(
        Font(R.font.josefinsans_bold, FontWeight.Black, FontStyle.Normal)
    )

//    var unselectableDatesUTC = remember { mutableStateListOf<LongRange>() }
    var unselectableDatesUTC : SnapshotStateList<LongRange> =
        remember {
            trip
                .cities
                .map {if (it.startTime != null && it.endTime != null) it.startTime!!..it.endTime!! else null}
                .filterNotNull()
                .toMutableStateList()
        }

    val updateTripCities: (Int, City) -> Unit = { index, updatedCity ->
        val newCities = tripCities.toMutableList()
        newCities[index] = updatedCity
        tripCities = newCities
    }

    // scroll state
    val scrollState = rememberLazyListState()

    // Function to add a new city
    val addNewCity: () -> Unit = {
        val newCity = City(
            cityId = UUID.randomUUID(),
            name = "",
            startTime = null,
            endTime = null,
            placeId = ""
        )
        tripCities = tripCities.toMutableList().apply { add(newCity) }
        expandedCities[newCity.cityId] = true
    }

    // Function to remove a city
    val removeCity: (City) -> Unit = { cityToRemove ->
        tripCities = tripCities.toMutableList().apply {
            removeAll { it.cityId == cityToRemove.cityId }
        }
    }

    // Container for page
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 16.dp),

        ) {
    // Overall Column start
    Column(modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .padding(20.dp)
    ) {
        // MY TRIPS column start
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Edit Trip",
                fontSize = 40.sp, textAlign = TextAlign.Center, modifier=Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                fontFamily = josefinsans,
            )
        }
        // MY TRIPS column end
        // Pull up menu column start
        Column(
            modifier = Modifier
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        )
        {
            CustomTextField(
                value = tripName,
                onValueChange = { tripName = it },
                placeholder = ( "Trip Name" ),
                modifier = Modifier
                    .fillMaxWidth(),
            )

            CustomTextField(
                value = tripDesc,
                onValueChange = { tripDesc = it },
                placeholder = ( "Trip Description" ),
                modifier = Modifier.padding(bottom = 10.dp)
                    .fillMaxWidth(),
            )
            BoxWithConstraints {
                val maxHeight = constraints.maxHeight

                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxHeight.dp - 200.dp)
                        .padding(bottom = 75.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tripCities.size) { index ->
                        val currentCity = tripCities[index]
                        val isExpanded = expandedCities[currentCity.cityId] ?: false
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(25.dp))
                                    .background(color = ValiseLightGray)
                                    .padding(10.dp)
                                    .clickable {
                                        expandedCities[currentCity.cityId] =
                                            !(expandedCities[currentCity.cityId] ?: false)
                                    },
                                verticalAlignment = Alignment.CenterVertically

                            ) {
                                Text(
                                    text = currentCity.name,
                                    fontSize = 18.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                // Remove city button
                                IconButton(
                                    onClick = { removeCity(currentCity) },
                                    modifier = Modifier.weight(0.2f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove City"
                                    )
                                }

                                // expand button
                                IconButton(
                                    onClick = {
                                        expandedCities = expandedCities.toMutableMap().apply {
                                            put(currentCity.cityId, !isExpanded)
                                        }
                                    },
                                    modifier = Modifier.weight(0.2f)
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded)
                                            Icons.Default.KeyboardArrowUp
                                        else
                                            Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand/collapse"

                                    )
                                }
                            }
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically(animationSpec = tween(300)),
                                exit = shrinkVertically(animationSpec = tween(300))
                            ) {
                                AddCityBox(
                                    city = currentCity,
                                    cityId = currentCity.cityId,
                                    placeId = currentCity.placeId,
                                    onPlaceIdChange = { newPlaceId ->
                                        updateTripCities(
                                            index,
                                            currentCity.copy(placeId = newPlaceId)
                                        )
                                    },
                                    updateCities = { updatedCity ->
                                        updateTripCities(index, updatedCity)
                                    },
                                    openFile = { null },
                                    updateCoverImage = {},
                                    tripId = trip.tripId,
                                    unselectableDatesUTC = unselectableDatesUTC
                                )
                            }
                        }
                    }

                    // Add City button
                    item {
                        FilledTonalButton(
                            onClick = addNewCity,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(25.dp))
                                .fillMaxWidth().height(70.dp)
                                .background(ValiseLightGray),
                            colors = ButtonColors(
                                containerColor = ValiseDarkGray,
                                contentColor = ValiseDarkDarkGray,
                                disabledContainerColor = ValiseDarkGray,
                                disabledContentColor = ValiseDarkDarkGray)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.plus_icon),
                                contentDescription = "Add Trip Icon",
                                modifier = Modifier.size(14.dp),
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Add City",
                                color = Color.Black,
                            )
                        }
                    }
                }
            }
        }
        // Pull up menu column end
    } // Overall column end


        // Overall column end
        // Gradient
        val isScrolledToBottom by remember {
            derivedStateOf {
                scrollState.canScrollForward.not()
            }
        }

        // Gradient
        if (!isScrolledToBottom) {
            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Transparent, Color.White),
                            )
                        )
                )
                Spacer(
                    modifier = Modifier.height(90.dp)
                )
            }
        }


        val isScrolledToTop by remember {
            derivedStateOf {
                scrollState.canScrollBackward.not()
            }
        }

        // Gradient
        if (!isScrolledToTop) {
            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
            ) {
                Spacer(
                    modifier = Modifier.height(270.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White, Transparent, ),
                            )
                        )
                )
            }

        }

        Spacer (
            modifier = Modifier
                .height(10.dp)
        )

    Column(
        modifier = Modifier.fillMaxHeight().padding(20.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        FilledTonalButton(
            onClick = {
                val updatedTrip = trip.copy(
                    destination = tripName,
                    description = tripDesc,
                    startTime = tripCities.filter { it.startTime != null }
                        .minByOrNull { it.startTime!! }?.startTime,
                    endTime = tripCities.filter { it.endTime != null }
                        .maxByOrNull { it.endTime!! }?.endTime,
                    cities = tripCities
                )

                controller.invoke(
                    OverviewControllerEvent.Update,
                    hashMapOf("oldTrip" to trip, "updatedTrip" to updatedTrip)
                )

                if (tripCities.isNotEmpty()) {
                    val firstCityPlaceId = tripCities.toMutableList()[0].placeId
                    OverviewViewModel.fetchAndSetPhotoForTrip(
                        context,
                        tripId = trip.tripId.toString(),
                        placeId = firstCityPlaceId
                    )
                }
                navController.navigate(Home)
            },
            modifier = Modifier
                .fillMaxWidth().height(60.dp),


            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Color(0xFFA8DEEC),
                contentColor = Color.Black,
            ),

            ) {
            Text("Update Trip")
            }
        }
    }
}