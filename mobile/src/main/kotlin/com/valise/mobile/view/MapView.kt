package com.valise.mobile.view

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.valise.mobile.controller.LoginController
import com.valise.mobile.model.Model
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.valise.mobile.R
import com.valise.mobile.controller.MapController
import com.valise.mobile.ui.theme.josefinsans
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import java.util.UUID
import com.valise.mobile.ui.theme.OffWhite
import kotlinx.datetime.toJavaLocalDateTime

@Composable
fun MapView(
    tripId: UUID,
    mapViewModel: MapViewModel,
    mapController: MapController,
    navController: NavController,
    selectedEventId: UUID? = null
) {
    val josefinsans = FontFamily(
        Font(R.font.josefinsans_bold, FontWeight.Black, FontStyle.Normal)
    )

    val viewModel by remember { mutableStateOf(mapViewModel) }
    val controller by remember { mutableStateOf(mapController) }
    var isMapLoaded by remember { mutableStateOf(false) }
    var isFilterOpen by remember { mutableStateOf(false) }

    val tripEvents = remember { mapViewModel.getEventsForTrip(tripId) ?: emptyList() }
    val tripName = remember { mapViewModel.getTripName(tripId) ?: "Unknown Trip" }
    val tripDates = remember { mapViewModel.getDateRange(tripId) }

    val markerStateMap = remember { mutableMapOf<UUID, MarkerState>() }
    val cameraPositionState = rememberCameraPositionState()

    // for the custom infobox window
    val activeMarkerId = remember { mutableStateOf<UUID?>(null) }

    // to open google maps
    val context = LocalContext.current
    val onOpenMapsClicked: (LatLng) -> Unit = { position ->
        val gmmIntentUri = Uri.parse("https://www.google.com/maps?q=${position.latitude},${position.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        context.startActivity(mapIntent)
    }

    val defaultLocation = LatLng(43.6532, -79.3832)
    val defaultBounds = LatLngBounds.builder().include(defaultLocation).build()

    // to position camera
    val precomputedLatLngMap = remember(tripEvents) {
        tripEvents.mapNotNull { event ->
            event.location?.let {
                event.eventId to LatLng(it.latitude, it.longitude)
            }
        }.toMap()
    }
    val bounds = remember(tripEvents) {

        val builder = LatLngBounds.builder()
        var hasValidLocation = false

        tripEvents.forEach { event ->
            event.location?.let {
                builder.include(LatLng(it.latitude, it.longitude))
                hasValidLocation = true
            }
        }

        if (hasValidLocation) {
            builder.build()
        } else {
            defaultBounds
        }
    }

    LaunchedEffect(tripEvents, bounds, selectedEventId) {

//        if (selectedEventId != null ) {
//            activeMarkerId.value = selectedEventId
//            val eventLocation = tripEvents.find { event -> event.eventId == selectedEventId }?.location
//            if (eventLocation != null) {
//                val position = LatLng(eventLocation.latitude, eventLocation.longitude)
//                cameraPositionState.animate(
//                    CameraUpdateFactory.newLatLngZoom(position, 14f),
//                    durationMs = 1000
//                )
//            } else {
//                return@LaunchedEffect
//            }
//        } else

        if (tripEvents.isNotEmpty() && bounds != defaultBounds) {
                tripEvents.forEach { event ->
                    Log.d("NO LOCATION ON LAUNCH", "${event.location}") // this shows null
                    if (event.location != null) {
                        val latLng = precomputedLatLngMap[event.eventId]
                        if (latLng != null && !markerStateMap.contains(event.eventId)) {
                            markerStateMap[event.eventId] = MarkerState(position = latLng)
                        }
                    }
                }
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, 100),
                    durationMs = 1000
                )

                if (selectedEventId != null) {
                    tripEvents.find { it.eventId == selectedEventId && it.location != null }?.let {
                        activeMarkerId.value = selectedEventId

                        val selectedEvent = tripEvents.find { it.eventId == selectedEventId && it.location != null }
                        selectedEvent!!.location?.let { location ->
                            val position = LatLng(location.latitude, location.longitude)
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(position, 20f),
                                durationMs = 1000
                            )
                        }
                    }
                }
        } else {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(defaultLocation, 1f),
                durationMs = 1000
            )
        }
    }

    // for date filtering
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val filteredEvents = remember(selectedDate, tripEvents) {
        if (selectedDate == null) {
            tripEvents // all events if there is no date selected
        } else {
            tripEvents.filter { event ->
                event.startTime.date == selectedDate // check for equal date events
            }.sortedBy { it.startTime }
        }
    }

    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize()
//            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {

        Text(
            tripName, fontSize = 40.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp).padding(bottom = 18.dp),
            fontFamily = josefinsans, fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = true,
                    zoomGesturesEnabled = true,
                )
            ) {
            filteredEvents.forEach { event ->
                event.location?.let { location ->
                    val position = LatLng(location.latitude, location.longitude)
                    val isActive = activeMarkerId.value == event.eventId

                    CustomMarker(
                        position = position,
                        isActive = isActive,
                        onMarkerClick = {
                            activeMarkerId.value = if (isActive ) null else event.eventId
                        }
                    )
                }
            }
        }

        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            AnimatedVisibility(
                visible = activeMarkerId.value != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                Box (
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White,
                            RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp)
                        )
                        .padding(20.dp)
                ) {
                    activeMarkerId.value?.let { activeId ->
                        filteredEvents.find { it.eventId == activeId }?.let { activeEvent ->
                            Column(
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = activeEvent.eventName,
                                        fontSize = 20.sp,
                                        fontFamily = josefinsans,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 1.sp
                                    )
                                    IconButton(
                                        onClick = { activeMarkerId.value = null },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.x_close_button),
                                            contentDescription = "Close",
                                            tint = Color.Black
                                        )
                                    }
                                }

                                Text(
                                    text = activeEvent.description.ifBlank { "No description" },
                                    fontSize = 16.sp,
                                    lineHeight = 18.sp
                                )
                                Text(
                                    text = activeEvent.startTime.date.toString(),
                                    fontSize = 16.sp,
//                                    lineHeight = 1.sp
                                )
                                Text(
                                    text = if (activeEvent.endTime != null) {
                                        "${activeEvent.startTime.time} to ${activeEvent.endTime!!.time}"
                                    } else {
                                        "Untimed event"
                                    },
                                    fontSize = 16.sp,
//                                    lineHeight = 1.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Box(
                                    modifier = Modifier
                                        .background(
                                            OffWhite,
                                            RoundedCornerShape(25.dp)
                                        )
                                        .size(200.dp, 60.dp)
                                        .clickable {
                                            onOpenMapsClicked(
                                                LatLng(
                                                    activeEvent.location!!.latitude,
                                                    activeEvent.location!!.longitude
                                                )
                                            )
                                        }
                                ) {
                                    Text(
                                        text = "Open in Maps",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

            // filter button
            IconButton(
                onClick = { isFilterOpen = !isFilterOpen },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(15.dp)
                    .size(60.dp)
                    .background(Color.White, CircleShape)
            ) {
                if (isFilterOpen) {
                    Image(
                        painter = painterResource(id = R.drawable.x_close_button),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.filter),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            if (isFilterOpen) {
                CustomLazyDropdown(
                    items = tripDates,
                    modifier = Modifier
                        .padding(top = 70.dp) // Position below button
                        .padding(15.dp)
                        .width(150.dp),
                    onItemClicked = { date ->
                        Log.d("DATE SELECTED", "$date")
                        selectedDate = date
                    }
                ) { date ->
                    Text(
                        text = date.toString(), // Format date as needed
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
fun CustomMarker(
    position: LatLng,
    isActive: Boolean,
    onMarkerClick: () -> Unit,
) {
    Marker(
        state = rememberUpdatedMarkerState(position),
        onClick = {
            onMarkerClick()
            true
        }
    )
}

@Composable
fun rememberUpdatedMarkerState(newPosition: LatLng): MarkerState{
    return remember { MarkerState(position = newPosition) }
        .apply{position = newPosition}
}

@Composable
fun CustomInfoWindow(
    map: GoogleMap?,
    markerPosition: LatLng,
    title: String,
    description: String,
    isVisible: Boolean,
    onOpenMapsClicked: () -> Unit,
) {
    val screenPosition = remember(markerPosition) {
        map?.projection?.toScreenLocation(markerPosition)
    }

    if (screenPosition != null) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(screenPosition.x - 100, screenPosition.y - 200)
                }
                .background(Color.White, RoundedCornerShape(25.dp))
                .padding(8.dp)
        ) {

        }
    }
}

@Composable
fun <T> CustomLazyDropdown(
    items: List<T>,
    modifier: Modifier = Modifier,
    onItemClicked: (T) -> Unit,
    maxDropdownHeight: Dp = 200.dp,
    itemContent: @Composable (T) -> Unit, // this allows you to format your text however
) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(25.dp))
            .heightIn(max = maxDropdownHeight) // limit height to make it scrollable
            .padding(8.dp)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items) { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = true, radius = 100.dp)
                        ) { onItemClicked(item) }

                ) {
                    itemContent(item)
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewMapView() {
    val model = Model()
    val viewModel = MapViewModel(model)
    val controller = MapController(model)
}


