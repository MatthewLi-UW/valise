package com.valise.mobile.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.valise.mobile.Home
import com.valise.mobile.R
import com.valise.mobile.controller.OverviewController
import com.valise.mobile.entities.City
import com.valise.mobile.entities.DocumentTypes
import com.valise.mobile.entities.Trip
import com.valise.mobile.entities.TripDocument
import com.valise.mobile.entities.TripEvent
import com.valise.mobile.ui.theme.Transparent
import com.valise.mobile.ui.theme.ValiseDarkDarkGray
import com.valise.mobile.ui.theme.ValiseDarkGray
import com.valise.mobile.ui.theme.ValiseLightGray
import kotlinx.datetime.toJavaLocalDate
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.UUID

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun TripAddColumn(
    testColumnViewModel: OverviewViewModel,
    testColumnController: OverviewController,
    navController: NavController
) {
    val viewModel by remember { mutableStateOf(testColumnViewModel) }
    val controller by remember { mutableStateOf(testColumnController) }
    var tripName by remember { mutableStateOf("") }
    var tripDesc by remember { mutableStateOf("") }
    var showAddCity by remember { mutableStateOf(false) }
    var tripCities by remember { mutableStateOf(listOf<City>()) }

    val context = LocalContext.current
    val josefinsans = FontFamily(
        Font(R.font.josefinsans_bold, FontWeight.Black, FontStyle.Normal),)

    var unselectableDatesUTC = remember { mutableStateListOf<LongRange>() }

    // for image
    var placeId by remember {mutableStateOf("")}

    val updateTripCities: (Int, City) -> Unit = { index, updatedCity ->
        tripCities = tripCities.toMutableList().apply {
            this[index] = updatedCity
        }
    }

    // scroll state
    val scrollState = rememberLazyListState()

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
                    text = "Add a Trip",
                    fontSize = 40.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = josefinsans,
                )
            }
            // MY TRIPS column end
            // Pull up menu column start
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                CustomTextField(
                    value = tripName,
                    onValueChange = { tripName = it },
                    placeholder = "Trip Name",
                    modifier = Modifier.fillMaxWidth(),
                )

                CustomTextField(
                    value = tripDesc,
                    onValueChange = { tripDesc = it },
                    placeholder = "Trip Description (optional)",
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .fillMaxWidth(),
                )

                // used BoxWithConstraints to track scroll height
                BoxWithConstraints (
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                ){
                    val maxHeight = constraints.maxHeight

                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = maxHeight.dp - 200.dp)
                            .padding(bottom = 75.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (tripCities.isNotEmpty()) {
                            items(tripCities.size) { i ->
                                val currentCity = tripCities[i]
                                AddCityBox(
                                    city = currentCity,
                                    cityId = currentCity.cityId,
                                    placeId = currentCity.placeId,
                                    tripId = UUID.randomUUID(),
                                    onPlaceIdChange = { newPlaceId ->
                                        updateTripCities(i, tripCities[i].copy(placeId = newPlaceId))
                                    },
                                    updateCities = { updatedCity ->
                                        updateTripCities(i, updatedCity)
                                    },
                                    openFile = { null },
                                    updateCoverImage = {},
                                    unselectableDatesUTC = unselectableDatesUTC
                                )
                            }
                        }

                        // Add City button
                        item {
                            FilledTonalButton(
                                onClick = {
                                    tripCities += City(
                                        cityId = UUID.randomUUID(),
                                        name = "",
                                        startTime = null,
                                        endTime = null,
                                        placeId = "",
                                    )
                                },
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
        }

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

        // Done button
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(20.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            FilledTonalButton(
                onClick = {
                    val tripId = UUID.randomUUID()
                    controller.invoke(
                        OverviewControllerEvent.AddTrip,
                        Trip(
                            tripId = tripId,
                            destination = tripName,
                            startTime = tripCities.filter { it.startTime != null }
                                .minByOrNull { it.startTime!! }?.startTime,
                            endTime = tripCities.filter { it.endTime != null }
                                .maxByOrNull { it.endTime!! }?.endTime,
                            description = tripDesc,
                            backgroundImage = R.drawable.nyc,
                            cities = tripCities.toMutableList(),
                            documentList = mutableListOf(),
                            eventList = emptyList<TripEvent>().toMutableList()
                        )
                    )
                    if (tripCities.isNotEmpty()) {
                        val firstCityPlaceId = tripCities.toMutableList()[0].placeId
                        OverviewViewModel.fetchAndSetPhotoForTrip(
                            context,
                            tripId = tripId.toString(),
                            placeId = firstCityPlaceId
                        )
                    }
                    navController.navigate(Home)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFA8DEEC),
                    contentColor = Color.Black,
                ),
            ) {
                Text("Done")
            }
        }
    }
} // Function End

@Composable
fun AddCityBox(
    city: City?,
    cityId: UUID,
    placeId: String,
    tripId: UUID,
    onPlaceIdChange: (String) -> Unit,
    updateCities: (City) -> Unit,
    openFile: () -> File?,
    updateCoverImage: (City) -> Unit,
    unselectableDatesUTC: MutableList<LongRange>,
) {
    var cityName by rememberSaveable { mutableStateOf(city?.name ?: "") }
    var startTime by rememberSaveable { mutableStateOf(city?.startTime ?: null) }
    var endTime by rememberSaveable { mutableStateOf(city?.endTime ?: null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
   // var placeId by remember {mutableStateOf<String>("")} ///////// PROBLEM IS HERE <-----------------

    Column (
        modifier = Modifier
            .shadow(
            elevation = 10.dp,
            shape = RoundedCornerShape(35.dp)
        ).background(ValiseLightGray).padding(16.dp)
    ){
        CityAutoComplete(
            cityName = cityName,
            onChange = {
                Log.e("AddTripMenu", "placeId update runs here1 $placeId")
                cityName = it
                updateCities(City(
                    name = cityName,
                    startTime =  startTime,
                    endTime = endTime,
                    cityId = cityId,
                    placeId = placeId
                ))
            },
            onSelect = {selectedCity ->
                Log.e("AddTripMenu", "placeId update runs here2 $placeId")
                cityName = selectedCity
                updateCities(City(
                    name = cityName,
                    startTime =  startTime,
                    endTime = endTime,
                    cityId = cityId,
                    placeId = placeId,
                ))
            },
            context = LocalContext.current,
            tripId = tripId,
            fetchPhoto = {
                         tripId, newPlaceId ->
                onPlaceIdChange(newPlaceId)
                OverviewViewModel.fetchAndSetPhotoForTrip(context, tripId.toString(), newPlaceId)
            },
            updateCities = updateCities

        )
        Row (
            modifier = Modifier.fillMaxWidth().clickable(onClick = {showDatePicker = true}),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ){
            //https://developer.android.com/develop/ui/compose/components/datepickers#range-key
            var startTimeString = "Start Date"
            var endTimeString = "End Date"
            if (startTime != null) {
                val date = convertNullableTimestampToLocalDateInUTC(startTime)
                val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                startTimeString = date!!.toJavaLocalDate().format(formatter)
            }

            if (endTime != null) {
                val date = convertNullableTimestampToLocalDateInUTC(endTime)
                val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                endTimeString = date!!.toJavaLocalDate().format(formatter)
            }
            CustomTextBox(
                placeholder = startTimeString,
                modifier = Modifier.weight(2f)
            )
            Image(
                painter = painterResource(id = R.drawable.arrow),
                contentDescription = null,
                modifier = Modifier.weight(1f).padding(6.dp)
            )
            CustomTextBox(
                placeholder = endTimeString,
                modifier = Modifier.weight(2f)
            )

//            OutlinedButton(onClick = {showModal = true}, modifier = Modifier.weight(1f)) {
//                Text("Edit Date")
//            }

            if(showDatePicker){
                if(startTime != null && endTime != null)
                    unselectableDatesUTC.remove(startTime!!..endTime!!)
                DateRangePickerModal(
                    onDateRangeSelected = {
                        startTime = it.first
                        endTime = it.second
                        showDatePicker = false
                        updateCities(City(
                            name = cityName,
                            startTime = startTime,
                            endTime = endTime,
                            cityId = cityId,
                            placeId = placeId
                        ))
                    },
                    onDismiss = {
                        showDatePicker = false
                        if(startTime != null && endTime != null)
                            unselectableDatesUTC.add(startTime!!..endTime!!)
                        // handles update cancel should recover the deleted date 18 lines above
                    },
                    unselectableDatesUTC = unselectableDatesUTC,
                )
            }
        }
    }
}

// function to add autocomplete to the search bar
@Composable
fun CityAutoComplete(
    cityName: String,
    onChange: (String) -> Unit,
    onSelect: (String) -> Unit,
    context: Context,
    tripId: UUID,
    fetchPhoto: (UUID, String) -> Unit,
    updateCities: (City) -> Unit
) {
    var suggestions by rememberSaveable { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var selectedSuggestion by rememberSaveable { mutableStateOf(false) }
    var placeId by rememberSaveable { mutableStateOf("") }
    val placesClient = remember { Places.createClient(context) }

    // fetch predictions when cityName changes
    LaunchedEffect(cityName) {
        if (cityName.isNotBlank() && !selectedSuggestion) {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(cityName)
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
            value = cityName,
            onValueChange = {
                onChange(it)
                selectedSuggestion = false
            },
            placeholder = ("City name") ,
            modifier = Modifier.fillMaxWidth().background(ValiseLightGray).padding(bottom = 10.dp),
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
                                onSelect(suggestionText) // put text in
                                placeId = suggestion.placeId
                                suggestions = emptyList() // clear suggestions on selection
                                selectedSuggestion = true
                                fetchPhoto(tripId, placeId)
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

@Composable
fun AddDocumentBox(
    documentId: UUID,
    eventId: UUID?,
    updateDocuments: (TripDocument, Uri) -> Unit,
    currentUserEmail: String,
) {
    var documentName by remember { mutableStateOf<String>("") }
    var documentType by remember { mutableStateOf<DocumentTypes>(DocumentTypes.Other) }
    var selectedDocumentUri by remember { mutableStateOf<Uri?>(null) }
    var selectedDocumentFile by remember { mutableStateOf<File?>(null) }
    var context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    fun getCityDocument(): TripDocument? {
        return if (documentName.isNotEmpty() && selectedDocumentUri != null) {
            selectedDocumentUri?.let { uri ->
                val mimeType: String? = context.contentResolver.getType(uri)
                TripDocument(
                    documentId = documentId,
                    fileName = documentName,
                    type = documentType,
                    eventId = eventId,
                    addedBy = currentUserEmail,
                    // https://stackoverflow.com/questions/8589645/how-to-determine-mime-type-of-file-in-android
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType).toString(),
                    mimeType = mimeType ?: "*/*",
                )
            }
        } else {
            null
        }
    }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            selectedDocumentUri = uri
            if(selectedDocumentUri != null)
                getCityDocument()?.let { updateDocuments(it, selectedDocumentUri!!) }
        }
    )

    Column (

        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        CustomTextField(
            value = documentName,
            onValueChange = {
                documentName = it
                if(selectedDocumentUri != null)
                    getCityDocument()?.let { updateDocuments(it, selectedDocumentUri!!) }
                // Essentially gets the composable's CityDocument if it's valid, and calls update with it
                // just to avoid the '!!'
            },
            placeholder = "Document Name",
        )
        Row (
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ){
            Box(modifier = Modifier.fillMaxHeight().weight(3f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expanded = !expanded
                        }
                        .border(3.dp, ValiseDarkDarkGray, RoundedCornerShape(25.dp))
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Localized description",
                        modifier = Modifier.padding(start = 10.dp))
                    Text(text = "$documentType",
                        fontSize = 15.sp
                        )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Hotel") },
                        onClick = { documentType = DocumentTypes.Hotel
                            expanded = false
                            if(selectedDocumentUri != null)
                                getCityDocument()?.let { updateDocuments(it, selectedDocumentUri!!) }
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Flight") },
                        onClick = { documentType = DocumentTypes.Flight
                            expanded = false
                            if(selectedDocumentUri != null)
                                getCityDocument()?.let { updateDocuments(it, selectedDocumentUri!!) }
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Bus") },
                        onClick = { documentType = DocumentTypes.Bus
                            expanded = false
                            if(selectedDocumentUri != null)
                                getCityDocument()?.let { updateDocuments(it, selectedDocumentUri!!) }
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Other") },
                        onClick = { documentType = DocumentTypes.Other
                            expanded = false
                            if(selectedDocumentUri != null)
                                getCityDocument()?.let { updateDocuments(it, selectedDocumentUri!!) }
                        },
                    )
                }
            }
            OutlinedButton (
                modifier = Modifier.fillMaxHeight(),
                onClick = {singlePhotoPickerLauncher.launch("*/*") },
                border = BorderStroke(3.dp, ValiseDarkDarkGray),
                colors = ButtonColors(
                    containerColor = ValiseDarkGray,
                    contentColor = ValiseDarkDarkGray,
                    disabledContainerColor = ValiseDarkGray,
                    disabledContentColor = ValiseDarkDarkGray
                )
            ) {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_document_black_24dp),
                    contentDescription = null,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }
    }
}


private val testEvent = Trip(
    UUID.randomUUID(),
    "Hamilton Test",
    startTime = 0,
    endTime = 0,
    description = "Trip to Hamilton!",
    backgroundImage = R.drawable.nyc,
    documentList = mutableListOf(),
    eventList = emptyList<TripEvent>().toMutableList()
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    onDismiss: () -> Unit,
    unselectableDatesUTC: List<LongRange>,
) {
    val dateRangePickerState = rememberDateRangePickerState(
        selectableDates = object : SelectableDates{
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                unselectableDatesUTC.forEach {
                        if(it?.contains(utcTimeMillis) == true)
                            return false
                }
                return true
            }
        },
    )
    // tried this for dynamically deselectable dates but didnt work:
    // https://stackoverflow.com/questions/78459136/how-can-i-dynamically-change-selectabledates-in-daterangepicker/79235598#79235598


    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled =
                    (dateRangePickerState.selectedStartDateMillis != null
                        && dateRangePickerState.selectedEndDateMillis != null),
                onClick = {
                    onDateRangeSelected(
                        Pair(
                            dateRangePickerState.selectedStartDateMillis,
                            dateRangePickerState.selectedEndDateMillis
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(top=16.dp, bottom=16.dp),
            colors = DatePickerDefaults.colors(
                dayInSelectionRangeContainerColor = Color(0xFFD0E8EF)
            )
        )
    }
}
