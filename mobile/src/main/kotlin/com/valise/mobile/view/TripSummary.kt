package com.valise.mobile.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import com.skydoves.cloudy.cloudy
import com.valise.mobile.MapView
import com.valise.mobile.R
import com.valise.mobile.controller.SummaryController
import com.valise.mobile.entities.DocumentTypes
import com.valise.mobile.entities.Trip
import com.valise.mobile.entities.TripDocument
import com.valise.mobile.entities.TripEvent
import com.valise.mobile.noRippleClickable
import com.valise.mobile.ui.theme.OffWhite
import com.valise.mobile.ui.theme.ValiseBlue
import com.valise.mobile.ui.theme.ValiseDarkDarkGray
import com.valise.mobile.ui.theme.ValiseDarkGray
import com.valise.mobile.ui.theme.ValiseLightGray
import com.valise.mobile.ui.theme.ValiseRed
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.util.UUID


//@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun TripSummaryList(
    trip: Trip,
    summaryViewModel: SummaryViewModel,
    summaryController: SummaryController,
    navController: NavController
) {
    val viewModel by remember { mutableStateOf(summaryViewModel) }
    val controller by remember { mutableStateOf(summaryController) }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var editingEvent by rememberSaveable { mutableStateOf(false) }
    var curEventEditing by remember { mutableStateOf<TripEvent?>(null) }
    var curEventId by remember { mutableStateOf<UUID>(UUID.randomUUID()) }
    var hamburgerExpanded by rememberSaveable {mutableStateOf(false)}
    var shareMenuOpen by rememberSaveable {mutableStateOf(false)}
    var documentMenuOpen by rememberSaveable {mutableStateOf(false)}
    var addEventDate by remember { mutableStateOf(LocalDate(2024,1,22))}  //just a dummy date but it doesn't matter cause it'll be changed when its used
    var curEventDocumentsShowing by remember { mutableStateOf<TripEvent?>(null) }
    val context = LocalContext.current

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // File was opened successfully
            Log.d("FileOpen", "File was opened successfully")
        } else {
            // Handle cancellation or errors
//            Toast.makeText(context, "Action cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    //    var eventsByDate = remember { mutableStateMapOf<LocalDate, TripEvent>()}
    val josefinsans = FontFamily(
        Font(R.font.josefinsans_bold, FontWeight.Black, FontStyle.Normal))

    val dates = dateRange(
        convertTimestampToLocalDateInUTC(trip.startTime ?: 0),
        convertTimestampToLocalDateInUTC(trip.endTime ?: 0))
    val state = rememberLazyListState()

    val isExpandedMap = remember { mutableStateMapOf<LocalDate, Boolean>() }


    Box(
        modifier =
            if( menuExpanded || documentMenuOpen || hamburgerExpanded || shareMenuOpen || editingEvent )
                Modifier.cloudy(radius = 60)
            else Modifier
    ) {
        IconButton(
            onClick = { hamburgerExpanded = true },
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 15.dp, end = 20.dp).zIndex(2f) // so its above the LazyColumn
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.Black,
                modifier = Modifier.size(50.dp)
            )
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.padding(20.dp, 0.dp, 20.dp, 0.dp).fillMaxSize(),
            state = state,
//            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            item {
                Text(trip.destination, fontSize = 40.sp, textAlign = TextAlign.Center,
                    modifier=Modifier.fillMaxWidth().padding(top=40.dp),
                    fontFamily = josefinsans, fontWeight = FontWeight.Bold)
            }

            item {
                val startDate = convertNullableTimestampToLocalDateInUTC(trip.startTime)
                val endDate = convertNullableTimestampToLocalDateInUTC(trip.endTime)

                val displayText = when {
                    startDate != null && endDate != null -> "$startDate–$endDate"
                    startDate != null -> startDate.toString()
                    endDate != null -> endDate.toString()
                    else -> "No Dates Set"
                }

                Text(
                    text = displayText,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontFamily = josefinsans
                )
            }

            items(dates.size, key = {dates[it].toString()}) { i ->
                val date = dates[i]
                DateBox(
                    localDate = date,
                    events = viewModel.getEventsByDate(trip.tripId)[date] ?: emptyList(),
                    locationString = viewModel.getLocationStringByDate(trip.tripId, date),
//                    eventsByDate.value[date] ?: emptyList(), //list of events under that day
//                    { event ->
//                        val currentEvents = eventsByDate.value[date] ?: mutableListOf() //if it dne make a list
//                        currentEvents.add(event) //add to list
//                        eventsByDate.value[date] = currentEvents //add to list of events for each date
//                    }, //add event
                    deleteEvent = { event ->
                    //                        val currentEvents = viewModel.getEventsByDate().values.toList()
                    //                        currentEvents.remove(event)
                    //                        eventsByDate.value[date] = currentEvents ?: mutableListOf()
                        controller.invoke(
                            TripSummaryControllerEvent.DeleteEvent,
                            hashMapOf("tripId" to trip.tripId, "event" to event)
                        )
                    }, //delete event
                    selected = {
                        menuExpanded = true
                        addEventDate = date
                    },
                    color = DateBoxColours.entries.getOrNull(i % 5)?.color ?: Color.Transparent,
                    detailView = false,
                    onEventSwapRequest = { a ->
                        controller.invoke(TripSummaryControllerEvent.SwapEvent, hashMapOf("trip" to trip, "orders" to a))
                    },
                    isExpanded = isExpandedMap[date] == true,
                    onExpandRequest = {
                        if(isExpandedMap[date] == null) isExpandedMap[date] = true
                        else
                            isExpandedMap[date]?.let { isExpandedMap[date] = !it }
                    },
                    editEvent = { event->
                        curEventEditing = event
                        curEventId = event.eventId
                        editingEvent = true
                    },
                    showDocumentsForEvent = { event ->
                        curEventDocumentsShowing = event
                        documentMenuOpen = true
                    },
                    tripId = trip.tripId,
                    navController = navController
                )
            }
            item {Spacer(modifier = Modifier.height(0.dp))}
        }
    }

    Box(
        modifier =
        if( menuExpanded || documentMenuOpen || hamburgerExpanded || shareMenuOpen || editingEvent )
            Modifier.fillMaxSize()
                .noRippleClickable{
                    hamburgerExpanded = false
                    menuExpanded = false
                    shareMenuOpen = false
                    documentMenuOpen = false
                }
        else Modifier
    ) {}

    AnimatedVisibility(
        visible = menuExpanded,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AddEventMenu(
            date = addEventDate,
            trip = trip,
            eventId = UUID.randomUUID(),
            onDismiss = { menuExpanded = false },
            onAddEvent = { event ->
                controller.invoke(
                    TripSummaryControllerEvent.AddEvent,
                    hashMapOf("tripId" to trip.tripId, "event" to event)
                )
            },
            onAddDocumentList = { trip, list ->
                list.forEach { document ->
                    @SuppressLint("Recycle") // I close it in the callback in the model
                    val inputStream = context.contentResolver.openInputStream(document.second)

                    controller.invoke(
                        TripSummaryControllerEvent.AddDocument,
                        hashMapOf(
                            "tripId" to trip.tripId,
                            "document" to document.first,
                            "inputStream" to inputStream
                        )
                    )
                }
            },
            onUpdateEvent = { a -> Log.wtf("TRIP Summary", "not supposed to be here") },
            event = null,
            onDeleteEvent = { a -> Log.wtf("TRIP Summary", "not supposed to be here") },
        )
    }

    AnimatedVisibility(
        visible = editingEvent,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AddEventMenu(
            date = LocalDate(2024, 1, 22),
//            visible = editEvent,
            trip = trip,
            eventId = curEventId,
            onDismiss = {editingEvent = false},
            onAddEvent = { event ->
                controller.invoke(TripSummaryControllerEvent.AddEvent, hashMapOf("tripId" to trip.tripId, "event" to event))
            },
            onAddDocumentList = { trip, list ->
                list.forEach { document ->
                    @SuppressLint("Recycle") // I close it in the callback in the model
                    val inputStream = context.contentResolver.openInputStream(document.second)

                    controller.invoke(TripSummaryControllerEvent.AddDocument,
                        hashMapOf("tripId" to trip.tripId,
                            "document" to document.first,
                            "inputStream" to inputStream
                        )
                    )
                }
            },
            event = curEventEditing,
            onUpdateEvent = { event ->
                controller.invoke(TripSummaryControllerEvent.UpdateEvent, hashMapOf("tripId" to trip.tripId, "event" to event))
            },
            onDeleteEvent = { event ->
                controller.invoke(TripSummaryControllerEvent.DeleteEvent, hashMapOf("tripId" to trip.tripId, "event" to event))
            }
        )
    }


    AnimatedVisibility(
        visible = hamburgerExpanded,
        enter = slideInHorizontally { it },
        exit = slideOutHorizontally { it },
    ) {
        HamburgerMenu(
            closeHamburger = { hamburgerExpanded = false },
            onSharePressed = { hamburgerExpanded = false; shareMenuOpen = true },
            onDocumentPressed = { hamburgerExpanded = false; curEventDocumentsShowing = null; documentMenuOpen = true },
            navController = navController,
            tripId = trip.tripId
        )
    }

    AnimatedVisibility(
        visible = shareMenuOpen,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ShareMenu(
            onShare = {email -> controller.invoke(TripSummaryControllerEvent.ShareTrip, Pair(trip, email)); shareMenuOpen = false},
            onDismiss = {shareMenuOpen = false}
        )
    }

    AnimatedVisibility(
        visible = documentMenuOpen,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
//        val reg = controller.invoke(TripSummaryControllerEvent.RegisterDocumentListener, trip.tripId) as ListenerRegistration
        DocumentMenu (
            context = context,
            eventFilter = curEventDocumentsShowing,
            documentList = viewModel.list.first { it.tripId == trip.tripId }.documentList,
            onFileOpenRequest = { url, fileName, mimeType ->
                viewModel.onFileOpenRequest(context, openFileLauncher, url, fileName, mimeType)
            },
//            onDismiss = {documentMenuOpen = false; reg.remove()},
            onDismiss = {documentMenuOpen = false},
            onAddDocumentPressed = { document, uri ->
                @SuppressLint("Recycle") // I close it in the callback in the model
                val inputStream = context.contentResolver.openInputStream(uri)
                controller.invoke(TripSummaryControllerEvent.AddDocument,
                    hashMapOf(
                        "tripId" to trip.tripId,
                        "document" to document,
                        "inputStream" to inputStream
                    ))
            },
            onDocumentDeleteRequest = { documentId ->
                controller.invoke(TripSummaryControllerEvent.DeleteDocument,
                    hashMapOf(
                        "tripId" to trip.tripId,
                        "documentId" to documentId,
                    ))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HamburgerMenu(
    closeHamburger: () -> Unit,
    onSharePressed: () -> Unit,
    onDocumentPressed: () -> Unit,
    navController: NavController,
    tripId: UUID
) {
    Box(Modifier.fillMaxSize()) {
        Column (modifier = Modifier.align(Alignment.TopEnd).padding(end = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)){
            IconButton(onClick = { closeHamburger() },
                modifier = Modifier.padding(top = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Black,
                    modifier = Modifier.size(50.dp),
                )
            }
            IconButton(onClick = { navController.navigate(route = MapView(tripId.toString())) },
                modifier = Modifier.background(ValiseBlue, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Maps",
                    tint = Color.Black,
                    modifier = Modifier.size(50.dp),
                )
            }
            IconButton(onClick = { onDocumentPressed() },
                modifier = Modifier.background(ValiseBlue, shape = CircleShape)
                ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_document_black_24dp),
                    contentDescription = "Documents",
                    modifier = Modifier.size(50.dp),
                )
            }
            IconButton(onClick = { onSharePressed() },
                modifier = Modifier.background(ValiseBlue, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Maps",
                    tint = Color.Black,
                    modifier = Modifier.size(50.dp),
                )
            }
        }
    }
}


//@Composable
//fun RowColor(selected: Boolean) = if (selected) Color.Yellow else Color.Transparent


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DateBox(
    localDate: LocalDate,
    events: List<TripEvent>,
    locationString: String,
    deleteEvent: (TripEvent) -> Unit,
    selected: () -> Unit, //Add Event is opened
    color: Color,
    detailView: Boolean,
    onEventSwapRequest: (orders: List<UUID>) -> Unit,
    onExpandRequest: () -> Unit,
    editEvent: (TripEvent) -> Unit,
    showDocumentsForEvent: (TripEvent) -> Unit,
    isExpanded: Boolean,
    tripId: UUID,
    navController: NavController,
) {
    val josefinsans = FontFamily(
        Font(R.font.josefinsans_bold, FontWeight.Black, FontStyle.Normal))

    Column(
        modifier = Modifier
            .animateContentSize()
            .clip(RoundedCornerShape(25.dp))
            .fillMaxWidth()
            .background(color)
            .padding(20.dp)
    )
    {
        //Title part of the box
        Row(
            modifier = Modifier.fillMaxWidth()
                .noRippleClickable (onExpandRequest),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ){
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = dateToWord(localDate),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = josefinsans)
                Text(
                    text = locationString,
                    color = Color.White,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 10.dp)
                )
            }
            if(isExpanded){
                IconButton(
                    onClick = { selected() },
                    modifier = Modifier.weight(0.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Event",
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
            else Spacer(modifier = Modifier.weight(0.2f))
        }

        if(!isExpanded){ //collapsed
            Text(
                text = buildString {
                    if ((events.getOrNull(0)?.eventName ?: "").isNotBlank()) append((events.getOrNull(0)?.eventName ?: ""))
                    if ((events.getOrNull(1)?.eventName ?: "").isNotBlank()) append(", ").append((events.getOrNull(1)?.eventName ?: ""))
                    if ((events.getOrNull(2)?.eventName ?: "").isNotBlank()) append(", ").append((events.getOrNull(2)?.eventName ?: ""))
                    if ((events.getOrNull(3)?.eventName ?: "").isNotBlank()) append("...")
                }.ifEmpty { "No events" },
                overflow = TextOverflow.Ellipsis,
                fontSize = 15.sp,
                color = Color.White,
                maxLines = 1
            )
        }else{ //expanded
            ReorderableEventsList(
                eventList = events,
                onEventSwapRequest = onEventSwapRequest,
                editEvent = editEvent,
                showDocumentsForEvent = showDocumentsForEvent,
                tripId = tripId,
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EventBox(
    event: TripEvent,
    modifier: Modifier = Modifier,
    editEvent: (TripEvent) -> Unit,
    showDocumentsForEvent: (TripEvent) -> Unit,
    tripId: UUID,
    navController: NavController
){
    var isExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "")

    Column(
        modifier =
        modifier
            .clip(RoundedCornerShape(25.dp))
            .fillMaxWidth()
            .indication(interactionSource, LocalIndication.current)
            .background(Color.LightGray)
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp, top = 10.dp)
    )
    {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ){
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top
            ){
                Text(
                    text = event.eventName,
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold

                )

                if (event.locationStr != ""){
//                    AnimatedVisibility(
//                        visible = isExpanded,
//                    ){
                        Text(
                            text = event.locationStr,
                            color = Color.Black,
                            fontSize = 18.sp,
                            maxLines = if(isExpanded) 10 else 2,
                            modifier = Modifier.animateContentSize()
                        )
//                    }
//                    AnimatedVisibility(
//                        visible = !isExpanded,
//                    ){
//                        Text(
//                            text = event.locationStr,
//                            overflow = TextOverflow.Ellipsis,
//                            color = Color.Black,
//                            fontSize = 18.sp,
//                            maxLines = 2
//                        )
//                    }
                }

                if (event.isTimed && event.endTime != null){
                    Row(){
                        Text(
                            text = "${event.startTime.time} to ${event.endTime!!.time}",
                            fontStyle = FontStyle.Italic,
                            fontSize = 18.sp,
                        )
                    }
                } else {
                    Row(){
                        Text(
                            text = "Untimed event",
                            fontStyle = FontStyle.Italic,
                            fontSize = 18.sp,
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(),
                    exit =  fadeOut()
                ){
                    Text(
                        text = if(event.description.isNotBlank()) event.description else "No description for event.",
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 15.sp,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, top = 1.dp),
//                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    modifier = Modifier.size(28.dp),
                    onClick = {
                        isExpanded = !isExpanded
                    },
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.dropdown),
                        contentDescription = null,
                        modifier = Modifier.size(19.dp).rotate(rotation)
                    )
                }
                IconButton(
                    modifier = Modifier.size(30.dp),
                    onClick = {
                        editEvent(event)
                    },
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_edit_24),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp)
                    )
                }
                IconButton(
                    modifier = Modifier.size(30.dp),
                    onClick = {
                        showDocumentsForEvent(event)
                    },
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_document_grey_24dp),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(
                    modifier = Modifier.size(25.dp),
                    onClick = { navController.navigate(route = MapView(tripId.toString(), event.eventId.toString())) },
                    ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Go to Location",
                        tint = ValiseDarkDarkGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }


        }



    }
}

data class DraggableItem(val index: Int)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReorderableEventsList(
    eventList: List<TripEvent>,
    onEventSwapRequest: (orders: List<UUID>) -> Unit,
    editEvent: (TripEvent) -> Unit,
    showDocumentsForEvent: (TripEvent) -> Unit,
    tripId: UUID,
    navController: NavController,
) {
    var draggingItem :LazyListItemInfo? by remember {mutableStateOf(null)}
    var draggingItemIndex by remember { mutableStateOf<Int?>(null)} //stores index of eventbox being dragged
    var initialItemIndex by remember { mutableStateOf<Int?>(null)}
    var delta: Float by remember { mutableFloatStateOf(0f) }
    var startDragY: Float by remember { mutableFloatStateOf(0f) }
//    var isExpandedMap = remember { mutableStateMapOf<UUID, Boolean>() }
    val stateList = rememberLazyListState()

    var localEventState by remember {mutableStateOf<List<TripEvent>>(eventList)}
//    Log.d("TRIPSUMMARY", "out recomp")

    LaunchedEffect(key1 = eventList) {
        localEventState = eventList.toMutableStateList()
    }
//
    fun onVisualMove(fromIndex: Int, toIndex:Int) {
        val tempMutableList = localEventState.toMutableList()
        tempMutableList.add(toIndex, tempMutableList.removeAt(fromIndex))
        localEventState = tempMutableList
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 5000.dp)
            .pointerInput(key1 = stateList) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        stateList.layoutInfo.visibleItemsInfo
                            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
                            ?.also {
                                (it.contentType as? DraggableItem)?.let { draggableItem ->
                                    draggingItemIndex = draggableItem.index
                                    initialItemIndex = draggableItem.index
                                    draggingItem = it
                                }
                            }
                        startDragY = offset.y
                    },
                    onDrag = { change, offset -> //
                        change.consume() //consume the drag event - prevents it from being handled by other components
                        delta += offset.y
                        val currentDraggingItemIndex =
                            draggingItemIndex ?: return@detectDragGesturesAfterLongPress
                        val currentDraggingItem =
                            draggingItem ?: return@detectDragGesturesAfterLongPress

                        val startOffset = currentDraggingItem.offset + delta
                        val endOffset =
                            currentDraggingItem.offset + currentDraggingItem.size + delta
                        val middleOffset = startOffset + (endOffset - startOffset) / 2

                        val targetItem =
                            stateList.layoutInfo.visibleItemsInfo.find { item ->
                                middleOffset.toInt() in item.offset..item.offset + item.size &&
                                        currentDraggingItem.index != item.index &&
                                        item.contentType is DraggableItem
                            }

                        if (targetItem != null) {
                            val targetIndex = (targetItem.contentType as DraggableItem).index
                            onVisualMove(currentDraggingItemIndex, targetIndex)
                            draggingItemIndex = targetIndex
                            draggingItem = targetItem
                            delta += currentDraggingItem.offset - targetItem.offset
                        }
                    },
                    onDragEnd = { //Reorder after drag
                        //if something is dragged and the idx of dragged item is not where it is hovered, reorder
//                        overIndex = stateList.layoutInfo.visibleItemsInfo
//                            .firstOrNull { item -> (delta + startDragY).toInt() in item.offset..(item.offset + item.size) }?.index
//                        Log.d("DRAG", "draggedIndex: $draggingItemIndex, overIndex: $overIndex" )
//                        if (draggingItemIndex != null && overIndex != null && draggingItemIndex != overIndex) {
//                            eventState.add(overIndex!!, eventState.removeAt(draggingItemIndex!!)) //remove dragged item from original position
//                            //OnReorder(events.toList())
//                        }
//                        //reset dragged index and hover index
                        if(initialItemIndex != null && draggingItemIndex != null) {
                            onEventSwapRequest(localEventState.map{it.eventId})
                        }

                        initialItemIndex = null
                        draggingItemIndex = null
                        draggingItem = null
                        delta = 0f
                        startDragY = 0f
                    },
                    onDragCancel = { //if drag gesture starts but isn't completed, reset indices
                        draggingItemIndex = null
                        draggingItem = null
                        delta = 0f
                        startDragY = 0f
                    }
                )
            },
        userScrollEnabled = false,
        state = stateList
    ){
        if (localEventState.isNotEmpty()){
//            Log.d("TRIPSUMMARY", "in recomp")
//            val sortedEvents = events.sortedWith(
//                compareBy<TripEvent>{
//                    if (it.startTime.isBlank()) "" else LocalTime.parse(it.startTime.padStart(5,'0'))}
//            )
            itemsIndexed(
                items = localEventState,
                contentType = { index, i -> DraggableItem(index = index) },
                key = { _, b -> b.eventId }
            ) { index, item ->
                EventBox(
                    event = item,
                    modifier = if(draggingItemIndex == index)
                        Modifier
                            .zIndex(1f)
                            .graphicsLayer { translationY = delta }
                        else Modifier.animateItem(),
                    editEvent = editEvent,
                    tripId = tripId,
                    navController = navController,
                    showDocumentsForEvent = showDocumentsForEvent
                )
            }

        } else{
            item{
                Text("Uneventful day", fontSize = 20.sp, color = Color.White, textAlign = TextAlign.Center, modifier=Modifier.fillMaxWidth())
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventMenu(
    date: LocalDate,
    trip: Trip,
    eventId: UUID,
    onDismiss: () -> Unit,
    onAddEvent: (TripEvent) -> Unit,
    onUpdateEvent: (TripEvent) -> Unit,
    onDeleteEvent: (TripEvent) -> Unit,
    onAddDocumentList: (Trip, List<Pair<TripDocument, Uri>>) -> Unit,
    event: TripEvent? // only non-null for editing
) {
    val josefinsans = FontFamily(
        Font(R.font.josefinsans_bold, FontWeight.Black, FontStyle.Normal),
    )
    var eventName by remember { mutableStateOf(event?.eventName ?: "") }
    var location by remember { mutableStateOf(event?.locationStr ?: "") }
    var geoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var showDocuments by remember {mutableStateOf(false)}
    var documentList = remember { mutableStateListOf<Pair<TripDocument, Uri?>>() }
    var startTime by remember { mutableStateOf(event?.startTime)}
    var endTime by remember { mutableStateOf(event?.endTime) }
    var description by remember { mutableStateOf(event?.description ?: "") }
    var timed by remember {mutableStateOf(event?.isTimed ?: true)}
    val currentUserEmailOrBlank = remember { Firebase.auth.currentUser?.email ?: "" }
    val context = LocalContext.current

    // Time picker: https://developer.android.com/develop/ui/compose/components/time-pickers
    var showStartTimePicker by remember {mutableStateOf<Boolean>(false)}
    var showEndTimePicker by remember {mutableStateOf<Boolean>(false)}

    var eventError by remember {mutableStateOf(false)}
    var timedError by remember {mutableStateOf(false)}
    var DoneOnce by remember {mutableStateOf(false)}
    val startTimePickerState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = false,
    )
    val endTimePickerState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = false,
    )

    /** Determines whether the time picker is dial or input */
    var showDial by remember { mutableStateOf(true) }

    /** The icon used for the icon button that switches from dial to input */
    val toggleIcon = if (showDial) {
        ImageVector.vectorResource(R.drawable.ic_edit_calendar)
    } else {
        ImageVector.vectorResource(R.drawable.ic_schedule)
    }

    val rotation by animateFloatAsState(targetValue = if (showDocuments) 180f else 0f, label = "")
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(27.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.animateContentSize()
                .background(OffWhite, shape = RoundedCornerShape(25.dp))
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 5.dp),
            ) {
                if (event == null) {
                    Text (
                        text = "Add event",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = josefinsans,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Text(
                        text = "Edit event",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = josefinsans,
                        modifier = Modifier.weight(1f),
                    )
                }

                IconButton(
                    onClick = { onDismiss() },
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.x_close_button),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            CustomTextField(
                isMandatory = true,
                value = eventName,
                onValueChange = { eventName = it },
                placeholder = "Event Name"
            )

            eventError = (eventName == "" && DoneOnce)
            if (eventError) {
                Text(
                    text = "This field is required",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }


            EventAutoComplete(
                    location = location,
            onChange = {
                location = it
            },
            onSelect = {selectedLocation, latLng ->
                location = selectedLocation //this is a string
                if (latLng != null) {
                    Log.e("Coordinates", "LAT LONG PLEASE PLEASE: $latLng")
                    geoPoint = latLng.let { GeoPoint(it.latitude, it.longitude) }
                }
            },
//                onChange = {
//                    location = it
//                    updateEvent(Event(
//                        name = cityName,
//                        eventId = eventId,
//                        placeId = placeId
//                    ))
//                },
//                onSelect = {selectedLocation ->
//                    location = selectedLocation
//                    updateEvent(Event(
//                          name = cityName,
//                        eventId = eventId,
//                        placeId = placeId
//                    ))
//                },
            context = LocalContext.current,
            eventId = eventId
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ){
                Text(
                    text = "Timed event",
                    color = Color.Black,
                    fontSize = 18.sp,
                )
                Checkbox(
                    checked = timed,
                    onCheckedChange = {
                        timed = !timed
                        timed = it
                    }
                )
            }
            if (timed){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CustomTextBox(
                        placeholder = if (startTime == null) "Start Time" else format.format(startTime!!.time),
                        modifier = Modifier.weight(2f).clickable(onClick = {showStartTimePicker = true})
                    )
                    timedError = (startTime == null && timed == true && DoneOnce)
                    Image(
                        painter = painterResource(id = R.drawable.arrow),
                        contentDescription = null,
                        modifier = Modifier.weight(0.7f).padding(6.dp)
                    )
                    CustomTextBox(
                        placeholder = if (endTime == null) "End Time" else format.format(endTime!!.time),
                        modifier = Modifier.weight(2f).clickable(onClick = {showEndTimePicker = true}),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(PaddingValues(top = 10.dp, start = 5.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    if (timedError) {
                        Text(
                            text = "This field is required",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
                var startTimeError by remember {mutableStateOf(false)}
                var endTimeError by remember {mutableStateOf(false)}
                var curTime by remember { mutableStateOf(event?.startTime) }

                if(showStartTimePicker || showEndTimePicker)
                    AdvancedTimePickerDialog(
                        onDismiss = {
                            showStartTimePicker = false
                            showEndTimePicker = false
                        },
                        onConfirm = {
                            if(showStartTimePicker) {
                                curTime = LocalDateTime(
                                    event?.startTime?.date ?: date,
                                    LocalTime(
                                        startTimePickerState.hour,
                                        startTimePickerState.minute
                                    )
                                )
                                if (endTime != null && curTime!! > endTime!!) {
                                    startTimeError = true
                                }else{
                                    startTimeError = false
                                }

                                if (!startTimeError) {
                                    startTime = curTime
                                    showStartTimePicker = false
                                }
                            }
                            if(showEndTimePicker) {
                                curTime = LocalDateTime(
                                    event?.startTime?.date ?: date,
                                    LocalTime(
                                        endTimePickerState.hour,
                                        endTimePickerState.minute
                                    )
                                )

                                if (startTime != null && (curTime!! < startTime!!)) {
                                    endTimeError = true
                                }else{
                                    endTimeError = false
                                }

                                if (!endTimeError) {
                                    endTime = curTime
                                    showStartTimePicker = false
                                    showEndTimePicker = false
                                }
                            }

                        },
                        toggle = {
                            IconButton(onClick = { showDial = !showDial }) {
                                Icon(
                                    imageVector = toggleIcon,
                                    contentDescription = "Time picker type toggle",
                                )
                            }
                        },

                    ) {
                        val valiseTimeColors = TimePickerColors(
                            clockDialColor = Color(0xFFD0E8EF), //super light blue
                            selectorColor = Color(0xff214457), //navy
                            containerColor = Color(0xFFA8DEEC), //light blue
                            periodSelectorBorderColor = Color(0xFFA8DEEC), //light blue
                            clockDialSelectedContentColor =  Color.White, //white
                            clockDialUnselectedContentColor = Color(0xFF214457), //navy
                            periodSelectorSelectedContainerColor = Color.White,
                            periodSelectorUnselectedContainerColor = Color(0xFFD0E8EF),
                            periodSelectorSelectedContentColor = Color(0xff214457),
                            periodSelectorUnselectedContentColor = Color.Black,
                            timeSelectorSelectedContainerColor = Color.White,
                            timeSelectorUnselectedContainerColor = Color(0xFFD0E8EF),
                            timeSelectorSelectedContentColor = Color(0xff214457),
                            timeSelectorUnselectedContentColor = Color.Black
                        )
                        val timePickerState = if (showStartTimePicker) startTimePickerState else endTimePickerState
                        if (showDial) {
                            TimePicker(
                                state = timePickerState,
                                colors = valiseTimeColors
                            )
                        } else {
                            TimeInput(
                                state = timePickerState,
                                colors = valiseTimeColors
                            )
                        }
                        if (endTimeError || startTimeError){
                            Row(
                                modifier = Modifier
                                    .height(20.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ){
                                if (endTimeError && showEndTimePicker) {
                                    Text(
                                        text = "End Time must be after Start Time",
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 16.dp)

                                    )
                                }
                                if (startTimeError && showStartTimePicker) {
                                    Text(
                                        text = "Start Time must be before End Time",
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }
                            }
                        }

                    }
            }


            CustomTextField(
                value = description,
                onValueChange = {description = it},
                placeholder = "Description (Optional)"
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(PaddingValues(top = 10.dp, start = 5.dp)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = {
                    if(documentList.isEmpty()) {
                        documentList += Pair(TripDocument(
                            documentId = UUID.randomUUID(),
                            fileName = "",
                            type = DocumentTypes.Other,
                            eventId = eventId,
                            addedBy = currentUserEmailOrBlank,
                            extension = "",
                            mimeType = "*/*"
                        ), null)
                    }
                    showDocuments = !showDocuments
                }){
                    Image(
                        painter = painterResource(id = R.drawable.dropdown),
                        contentDescription = null,

                        modifier = Modifier.size(15.dp).rotate(rotation)

                    )
                    Text(
                        text = "Add hotels, bookings, or tickets",
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = showDocuments,
            ) {
                Column {
                    Column {
                        documentList.forEachIndexed { i, it ->
                            AddDocumentBox(
                                documentId = it.first.documentId,
                                updateDocuments = { c: TripDocument, uri ->
                                    val mimeType: String? = context.contentResolver.getType(uri)
                                    documentList[i] = Pair(
                                        documentList[i].first.copy(
                                            fileName = c.fileName,
                                            type = c.type,
                                            // https://stackoverflow.com/questions/8589645/how-to-determine-mime-type-of-file-in-android
                                            extension = MimeTypeMap.getSingleton()
                                                .getExtensionFromMimeType(mimeType).toString(),
                                            mimeType = mimeType ?: "*/*",
                                        ), uri
                                    )
                                },
                                eventId = eventId,
                                currentUserEmail = currentUserEmailOrBlank,
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            documentList += Pair(
                                TripDocument(
                                    documentId = UUID.randomUUID(),
                                    fileName = "",
                                    type = DocumentTypes.Other,
                                    eventId = eventId,
                                    addedBy = currentUserEmailOrBlank,
                                    extension = "",
                                    mimeType = "*/*"
                                ), null
                            )
                        }
                    ) {
                        Text("Add another")
                    }
                }
            }

            // Button - when click done, make instance of an event
            if(event != null) FilledTonalButton(
                onClick = {
                    showDeleteConfirmDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),

                colors = ButtonColors(
                    containerColor = ValiseRed,
                    contentColor = Color.Black,
                    disabledContainerColor = ValiseDarkGray,
                    disabledContentColor = Color.Black
                )
            ){
                Text("Delete Event")
            }
            FilledTonalButton(
                onClick = {
                    DoneOnce = true
                    if (eventName == ""){
                        eventError = true
                    }
                    if (timed == true && startTime == null){
                        timedError = true
                    } else timedError = false
                    if (eventError == false && timedError == false){
                        val newEvent = TripEvent(
                            eventName = eventName,
                            location = geoPoint,
                            locationStr = location,
                            startTime = startTime ?: LocalDateTime(date, LocalTime(12, 0, 0, 0)),
                            endTime = endTime,
                            description = description,
                            isTimed = timed,
                            eventId = eventId,
                        )
                        if(event == null) onAddEvent(newEvent) //pass the new event to the parent
                        else onUpdateEvent(newEvent)
                        onAddDocumentList(trip, documentList.filter{it.second != null}.map{a -> Pair(a.first, a.second!!)})
                        onDismiss() //Close MENU
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),

                colors = ButtonColors(
                        containerColor = ValiseBlue,
                        contentColor = Color.Black,
                        disabledContainerColor = ValiseDarkGray,
                        disabledContentColor = Color.Black
            )
            ){
                Text("Done")
            }
        }
    }
    if (showDeleteConfirmDialog) {
        ConfirmDeleteEvent(
            trip = trip,
            event = event,
            onConfirm = {
                event?.let { onDeleteEvent(it) }
                onDismiss() //Close MENU

                showDeleteConfirmDialog = false
            },
            onDismiss = {
                showDeleteConfirmDialog = false
            }
        )
    }

}

@Composable
fun ConfirmDeleteEvent(
    trip: Trip,
    event: TripEvent?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (event == null) return
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "Delete Event", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Text("Are you sure you want to delete this event?")
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss() },

                colors = ButtonDefaults.buttonColors(containerColor = ValiseRed)
            ) {
                Text("YES", color = Color.White)
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("NO")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareMenu(
    onShare: (email: String) -> Unit,
    onDismiss: () -> Unit
) {
    val josefinsans = FontFamily(
        Font(R.font.josefinsans_bold, FontWeight.Black, FontStyle.Normal),
    )
    var email by remember { mutableStateOf<String>("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(27.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
//                .width(300.dp)
                .background(OffWhite, shape = RoundedCornerShape(25.dp))
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 5.dp),
            ) {
                Text(
                    text = "Share your itinerary",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontFamily = josefinsans,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { onDismiss() },
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.x_close_button),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            CustomTextField(
                isMandatory = true,
                value = email,
                onValueChange = { email = it },
                placeholder = "Email"
            )

            //Button - when click done, make instance of an event
            FilledTonalButton(
                onClick = {
                    onShare(email)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                colors = ButtonColors(
                    containerColor = ValiseBlue,
                    contentColor = ValiseDarkDarkGray,
                    disabledContainerColor = ValiseDarkGray,
                    disabledContentColor = ValiseDarkDarkGray
                )

            ){
                Text("Share")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentMenu(
    context: Context,
    documentList: List<TripDocument>,
    onFileOpenRequest: (url: String, fileName: String, mimeType: String) -> Unit,
    onDocumentDeleteRequest: (documentId: UUID) -> Unit,
    onDismiss: () -> Unit,
    onAddDocumentPressed: (document: TripDocument, uri: Uri) -> Unit,
    eventFilter: TripEvent?
) {
    val josefinsans = FontFamily(
        Font(R.font.josefinsans_bold, FontWeight.Black, FontStyle.Normal),
    )

    var showAddDocumentBox by remember {mutableStateOf(false)}
    var addedDocument by remember { mutableStateOf<TripDocument?>(null) }
    var addedDocumentUri by remember { mutableStateOf<Uri?>(null) }

    // for delete menu
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var documentToDelete by remember { mutableStateOf<TripDocument?>(null) }

    val filteredDocumentList =
        if(eventFilter != null)
            documentList.filter { it.eventId == eventFilter.eventId }
        else
            documentList

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(27.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn (
            modifier = Modifier
                .background(OffWhite, shape = RoundedCornerShape(25.dp))
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "Documents",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = josefinsans,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { onDismiss() },
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.x_close_button),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            item {
                AnimatedVisibility(
                    visible = showAddDocumentBox,
                    enter = fadeIn(),
                    exit =  fadeOut()
                ) {
                    Column {

                        AddDocumentBox(
                            documentId = UUID.randomUUID(),
                            eventId = eventFilter?.eventId,
                            updateDocuments = { document, uri ->
                                addedDocument = document
                                addedDocumentUri = uri
                            },
                            currentUserEmail = Firebase.auth.currentUser?.email ?: ""
                        )
                        TextButton(
                            //onClick = { onAddDocumentPressed(); onDismiss()},
                            onClick = {
                                val tempAddedDocument = addedDocument
                                val tempAddedDocumentUri = addedDocumentUri
                                if (tempAddedDocument == null || tempAddedDocumentUri == null)
                                    Toast.makeText(context, "Invalid document", Toast.LENGTH_SHORT)
                                        .show()
                                else {
                                    onAddDocumentPressed(tempAddedDocument, tempAddedDocumentUri)
                                    showAddDocumentBox = false;
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                //.border(BorderStroke(0.5.dp, Color.Gray), shape = MaterialTheme.shapes.medium)
                                .clip(MaterialTheme.shapes.medium),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = ValiseLightGray,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Add Icon",
                                modifier = Modifier.size(20.dp),
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Finish",
                                color = Color.Black,
                            )
                        }
                        if (filteredDocumentList.isNotEmpty()) {
                            HorizontalDivider(
                                thickness = 2.dp,
                                color = ValiseDarkDarkGray,
                                modifier = Modifier.clip(RoundedCornerShape(5.dp)).padding(top = 12.dp)
                            )
                        }
                    }
                }
                if(!showAddDocumentBox){
                    FilledTonalButton(
                        //onClick = { onAddDocumentPressed(); onDismiss()},
                        onClick = {showAddDocumentBox = true},
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .fillMaxWidth().height(50.dp)
                            .background(ValiseLightGray),
                        colors = ButtonColors(
                            containerColor = ValiseLightGray,
                            contentColor = ValiseDarkDarkGray,
                            disabledContainerColor = ValiseDarkGray,
                            disabledContentColor = ValiseDarkDarkGray),
//                        border = BorderStroke(0.dp, ValiseDarkDarkGray),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add Icon",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if(eventFilter == null) "Add a new Trip Document" else "Add a new Document to this Event",
                            color = Color.Black,
                        )
                    }
                }
            }
            items(filteredDocumentList.size){ i ->
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(35.dp))
                        //                        .shadow(
                        //                            elevation = 10.dp,
                        //                            shape = RoundedCornerShape(35.dp)
                        //                        )
                        .background(ValiseLightGray).clickable {
                            Log.d("a", "a")
                            onFileOpenRequest(
                                filteredDocumentList[i].documentId.toString() + '.' + filteredDocumentList[i].extension.toString(),
                                filteredDocumentList[i].fileName,
                                filteredDocumentList[i].mimeType)
                        }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Column (
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(start = 8.dp)
                    ){
                        Text(filteredDocumentList[i].fileName)
                        Text(filteredDocumentList[i].type.name, fontSize = 15.sp)
                        Text(filteredDocumentList[i].addedBy, fontSize = 12.sp)
                    }
                    IconButton (onClick = {
                        documentToDelete = filteredDocumentList[i]
                        showDeleteConfirmDialog = true
                    }){
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Menu",
                            tint = ValiseRed,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }
            item{
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
    if (showDeleteConfirmDialog) {
        documentToDelete?.let { document ->
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmDialog = false
                    documentToDelete = null
                },
                title = {
                    Text(text = "Delete Document", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("Are you sure you want to delete this document?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDocumentDeleteRequest(document.documentId)
                            showDeleteConfirmDialog = false
                            documentToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ValiseRed)
                    ) {
                        Text("YES", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showDeleteConfirmDialog = false
                        documentToDelete = null
                    }) {
                        Text("NO")
                    }
                }
            )
        }
    }
}

// event autocomplete
@Composable
fun EventAutoComplete(
    location: String,
    onChange: (String) -> Unit,
    onSelect: (String, LatLng?) -> Unit,
    context: Context,
    eventId: UUID,
) {
    var suggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    val placesClient = remember { Places.createClient(context) }
    var selectedSuggestion by remember { mutableStateOf(false) }
    var placeId by remember { mutableStateOf("") }

    // Fetch predictions when cityName changes
    LaunchedEffect(location) {
        if (location.isNotBlank() && !selectedSuggestion) {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(location)
                .build()
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    suggestions = response.autocompletePredictions

                }
                .addOnFailureListener { suggestions = emptyList() }
        } else if (selectedSuggestion) {
            selectedSuggestion = false // reset after selection
        } else {
            suggestions = emptyList()
        }
    }

    // TextField with autocomplete suggestions dropdown
    Column {
        CustomTextField(
            value = location,
            onValueChange = {
                onChange(it)
                selectedSuggestion = false
            },
            placeholder = ("Location search (Optional)"),
        )

        if (suggestions.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                for (suggestion in suggestions) {
                    val suggestionText = suggestion.getFullText(null).toString()
                    Text(
                        text = suggestion.getFullText(null).toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
//                                onSelect(suggestionText) // put text in
                                //smth = suggestion.getFullText(null).toString()
                                placeId = suggestion.placeId
                                fetchPlaceDetails(placesClient, placeId) { coordinates ->
                                    onSelect(suggestionText, coordinates) // Pass coordinates to parent
                                }
                                suggestions = emptyList() // clear suggestions on selection
                                selectedSuggestion = true
                            }
                            .padding(8.dp),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
// end of autocomplete part

fun fetchPlaceDetails(
    placesClient: PlacesClient,
    placeId: String,
    onResult: (LatLng?) -> Unit
) {
    val placeFields = listOf(Place.Field.LAT_LNG)
    val request = FetchPlaceRequest.newInstance(placeId, placeFields)

    placesClient.fetchPlace(request)
        .addOnSuccessListener { response ->
            val place = response.place
            val latLng = place.latLng
            onResult(latLng)
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField (
    value: String = "",
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    centerPlaceholder: Boolean = false,
    isMandatory: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {Text(
            text = placeholder,
            fontSize = 18.sp,
            color = Color.Black,
            textAlign = if (centerPlaceholder) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )},
        textStyle = TextStyle(
            fontSize = 18.sp, // Match placeholder font size
            color = Color.Black // Match placeholder color
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .border(3.dp, ValiseDarkDarkGray, RoundedCornerShape(25.dp)),
        shape = RoundedCornerShape(25.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.Black
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextBox (
    placeholder: String,
    modifier: Modifier = Modifier,
    centerPlaceholder: Boolean = false,
) {
    Text(
        text = placeholder,
        fontSize = 18.sp,
        color = Color.Black,
        textAlign = if (centerPlaceholder) TextAlign.Center else TextAlign.Start,
        modifier = modifier
            .fillMaxWidth()
            .border(3.dp, ValiseDarkDarkGray, RoundedCornerShape(25.dp))
            .padding(start = 18.dp, top= 15.dp, bottom = 15.dp),
    )
}

fun convertTimestampToLocalDateInUTC(timestamp: Long): LocalDate {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val localTimeZone = TimeZone.UTC
    val localDateTime = instant.toLocalDateTime(localTimeZone)
    return localDateTime.date
}

// https://developer.android.com/develop/ui/compose/components/time-pickers-dialogs
@Composable
fun AdvancedTimePickerDialog(
    title: String = "Select Time",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    toggle: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier =
            Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ),
            color = Color(0xffEDFAFE)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    toggle()
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text(text = "Cancel", color = Color(0xff214457)) }
                    TextButton(onClick = onConfirm) { Text(text = "OK", color = Color(0xff214457)) }

                }
            }
        }
    }
}

// https://kotlinlang.org/api/kotlinx-datetime/kotlinx-datetime/kotlinx.datetime.format/-date-time-format-builder/-with-time/am-pm-hour.html
val format = LocalTime.Format {
    amPmHour(); char(':'); minute(); char(':'); second()
    char(' '); amPmMarker("AM", "PM")
}


// types of actions that our controller understands
enum class TripSummaryControllerEvent { AddEvent, DeleteEvent, UpdateEvent, SwapEvent, AddDocument, DeleteDocument, ShareTrip, }

enum class DateBoxColours(val color: Color) {
    COLOUR1(Color(0xFF1B1E32)),
    COLOUR2(Color(0xFF313D68)),
    COLOUR3(Color(0xFF4E6D94)),
    COLOUR4(Color(0xFF6198A9)),
    COLOUR5(Color(0xFF80B0AD))
}

// Helper function to generate a list of days in a trip
fun dateRange(startDate: LocalDate, endDate: LocalDate) : List<LocalDate> {
    val dateRange = mutableListOf<LocalDate>()
    var currentDate = startDate
    val oneDay = DatePeriod(days = 1)
    while (currentDate <= endDate) {
        dateRange.add(currentDate)
        currentDate = currentDate.plus(oneDay)
    }
    return dateRange
}

fun getDaySuffix(day: Int): String{
    return when {
        day in 11..13 -> "${day}th"
        day % 10 == 1 -> "${day}st"
        day % 10 == 2 -> "${day}nd"
        day % 10 == 3 -> "${day}rd"
        else -> "${day}th"
    }
}

fun dateToWord(date: LocalDate): String {
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }

    val dayWithSuffix = getDaySuffix(date.dayOfMonth)
    return "$month $dayWithSuffix"
}
