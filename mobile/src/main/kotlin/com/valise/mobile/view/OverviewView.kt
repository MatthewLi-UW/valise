package com.valise.mobile.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.valise.mobile.Login
import com.valise.mobile.R
import com.valise.mobile.TripAdd
import com.valise.mobile.TripSummary
import com.valise.mobile.UpdateTrip
import com.valise.mobile.controller.OverviewController
import com.valise.mobile.entities.Trip
import com.valise.mobile.entities.TripEvent
import com.valise.mobile.ui.theme.ValiseBlue
import com.valise.mobile.ui.theme.ValiseDarkDarkGray
import com.valise.mobile.ui.theme.ValiseDarkGray
import com.valise.mobile.ui.theme.ValiseLightGray
import com.valise.mobile.ui.theme.ValiseRed
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

// types of actions that our controller understands
enum class OverviewControllerEvent { AddTrip, DeleteTrip, Share, Refresh, Logout, Update }

// Composable for overall trip list view
@Composable
fun OverviewList(
    overviewViewModel: OverviewViewModel,
    overviewController: OverviewController,
    navController: NavController
) {
    // model, controller
    val viewModel by remember { mutableStateOf(overviewViewModel) }
    val controller by remember { mutableStateOf(overviewController) }

    // font
    val josefinsans = FontFamily(Font(R.font.josefinsans_bold, FontWeight.Black, FontStyle.Normal))

    // container column
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp,),
        modifier = Modifier.fillMaxSize().background(Color.White).padding(20.dp, 0.dp, 20.dp, 0.dp),
    ) {
        // title + logout row
        item {
            Box() {

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text("My Trips", fontSize = 40.sp, textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontFamily = josefinsans)

            }
                Icon(
                    painter = painterResource(id = R.drawable.baseline_logout_24),
                    contentDescription = "Logout",
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 19.dp).size(40.dp)
                        .clickable {
                            controller.invoke(OverviewControllerEvent.Logout, 0)
                            navController.navigate(route = Login)
                        }
                )
            }
        }
        // "+ Add a new trip"
        item {
            AddTripButton {
                navController.navigate(TripAdd)
            }}

        // display trip boxes //
        items(viewModel.list.size, key = {viewModel.list[it].tripId}) { i ->
            val trip = viewModel.list[i]
            val context = LocalContext.current
            LaunchedEffect(trip.tripId) {
                if (trip.cities.isNotEmpty()) {
                    OverviewViewModel.fetchAndSetPhotoForTrip(
                        context, trip.tripId.toString(), trip.cities[0].placeId
                    )
                }
            }
            // find photos
            var photo = OverviewViewModel.tripImages[trip.tripId.toString()]
            if (photo == null) {
                photo = BitmapFactory.decodeResource(context.resources, R.drawable.defaultimage)
            }
            // trip box
            TripBox(
                trip = trip,
                photo = photo,
                {},
                {},
                {navController.navigate(route = TripSummary(trip.tripId.toString())) },
                modifier = Modifier.animateItem(),
                {navController.navigate(route = UpdateTrip(i)) },
                onDeleteTrip = { tripToDelete: Trip ->
                    // delete the trip from the model
                    viewModel.deleteTrip(tripToDelete) }

            )
        }
        item {
            Spacer(Modifier) // the spacedBy(24.dp) adds a space even though it takes 0 height
        }
    }
}

// delete confirmation dialog
@Composable
fun ConfirmDeleteDialog(
    trip: Trip,
    onConfirm: (Trip) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "Delete Trip", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Text("Are you sure you want to delete this trip?")
        },
        // delete
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(trip)
                    onDismiss() },

                colors = ButtonDefaults.buttonColors(containerColor = ValiseRed)
            ) {
                Text("YES", color = Color.White)
            }
        },
        // cancel
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("NO")
            }
        }
    )
}

// add trip button
@Composable
fun AddTripButton (onClick : () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(25.dp))
            .fillMaxWidth().height(70.dp)
            .background(ValiseLightGray),
        colors = ButtonColors(
            containerColor = ValiseBlue,
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
            text = "Add a new trip",
            color = Color.Black,
        )
    }
}

// generate each TripBox
@Composable
fun TripBox(
    trip: Trip,
    photo: Bitmap?,
    selected: () -> Unit,
    pressed: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onEditTrip: (Trip) -> Unit = {},
    onDeleteTrip: (Trip) -> Unit = {}

) {
    // font
    val josefinsans = FontFamily(Font(R.font.josefinsans_bold, FontWeight.Black, FontStyle.Normal))

    // edit trip dropdown
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // whole TripBox
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .height(150.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // display fetched photo
        if (photo != null) {
            Image(
                bitmap = photo.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
        }
        // Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(colorStops = colorStops))
                .clip(RoundedCornerShape(20.dp))
        )

        // menu to edit and delete trip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                // pencil icon
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Trip",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { showDropdownMenu = true }
                )

                // Dropdown menu
                DropdownMenu(
                    expanded = showDropdownMenu,
                    onDismissRequest = { showDropdownMenu = false },
                    modifier = Modifier
                        .background(Color.White)
                        .width(160.dp),
                    // change this to change where the box shows up
                    offset = DpOffset(196.dp, 4.dp)
                ) {
                    // Edit trip functionality
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Edit Trip",
                                color = Color.Black,
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            onEditTrip(trip)
                            showDropdownMenu = false
                        },
                        modifier = Modifier.height(40.dp)
                    )
                    // Delete trip functionality
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Delete Trip",
                                color = Color.Black,
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            showDeleteConfirmDialog = true
                            showDropdownMenu = false

                        },
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        }

        // All the text in the TripBox
        Column(
            modifier =Modifier.padding(20.dp).fillMaxSize()
        )
        {
            // Display trip name
            Text(
                trip.destination,
                color = Color.White,
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 7.dp),
                fontFamily = josefinsans,
                fontSize = 40.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // Display trip description
            Text(
                trip.description,
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth().padding(0.dp, 0.dp, 0.dp, 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Display start and end date
            Text(
                text = when {
                    // if both are not null, show dates
                    trip.startTime != null && trip.endTime != null ->
                        "${convertNullableTimestampToLocalDateInUTC(trip.startTime)} to ${convertNullableTimestampToLocalDateInUTC(trip.endTime)}"
                    // if one is null, only show the other (technically, it can only be start && !end)
                    trip.startTime != null ->
                        convertNullableTimestampToLocalDateInUTC(trip.startTime).toString()
                    trip.endTime != null ->
                        convertNullableTimestampToLocalDateInUTC(trip.endTime).toString()
                    else -> " " // blank for no dates
                },
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    // delete alert
    if (showDeleteConfirmDialog) {
        ConfirmDeleteDialog(
            trip = trip,
            onConfirm = {
                onDeleteTrip(trip)
                showDeleteConfirmDialog = false
            },
            onDismiss = {
                showDeleteConfirmDialog = false
            }
        )
    }
}

// For gradient
val colorStops = arrayOf(
    0.3f to Color(18,14,18,140),
    1f to Color(0,0,0,0)
)

// Convert a nullable timestamp (Long?) to nullable date (LocalDate?)
fun convertNullableTimestampToLocalDateInUTC(timestamp: Long?): LocalDate? {
    return timestamp?.let {
        val instant = Instant.fromEpochMilliseconds(it)
        val localTimeZone = TimeZone.UTC
        val localDateTime = instant.toLocalDateTime(localTimeZone)
        localDateTime.date
    }
}
