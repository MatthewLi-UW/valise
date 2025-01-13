package com.valise.mobile.entities

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp
import java.util.UUID
import kotlin.collections.mutableListOf

data class Trip(
    var tripId: UUID,
    var destination: String,
    var startTime: Long?,
    var endTime: Long?,
    var color: Color = Color.Blue,
    var description: String,
    var cities: MutableList<City> = mutableListOf<City>(),
    var backgroundImage: Int?,
    var backgroundImageUrl: String? = null,
    var backgroundImageBitmap: Bitmap? = null,
    var documentList: MutableList<TripDocument>,
    var eventList: MutableList<TripEvent>,
    var lastUpdatedAt: Long = 0,
)

data class FSTrip(
    var destination: String = "",
    var startTime: Timestamp? = null,
    var endTime: Timestamp? = null,
    var description: String = "",
    @field:JvmField
    var isActive: Boolean = false,
    var lastUpdatedAt: Long = 0,
)

fun FSTrip.toTrip(tripId: UUID, cities: MutableList<City>, documents: MutableList<TripDocument>, events: MutableList<TripEvent>) : Trip = Trip(
    tripId = tripId,
    destination = this.destination,
    startTime = this.startTime?.toDate()?.time,
    endTime = this.endTime?.toDate()?.time,
    description = this.description,
    cities = cities,
    backgroundImage = null,
    backgroundImageUrl = null,
    backgroundImageBitmap = null,
    documentList = documents,
    eventList = events,
    lastUpdatedAt = lastUpdatedAt,
)

fun Trip.toFSTrip() : FSTrip = FSTrip(
    destination = destination,
    startTime = if(startTime != null) Timestamp(startTime!!/1000, 0) else null,
    endTime = if(endTime != null) Timestamp(endTime!!/1000, 0) else null,
    description = description,
    isActive = true,
    lastUpdatedAt = lastUpdatedAt,
)


fun MutableList<Trip>.addTrip(element: Trip): Boolean {
    this.add(element)
    this.reindex()
    return true
}

fun MutableList<Trip>.updateTrip(element: Trip): Boolean {
    // Find the index of the existing trip with the same tripId
    val existingTripIndex = indexOfFirst { it.tripId == element.tripId }
    this[existingTripIndex] = element
    this.reindex()
    return true
}

fun MutableList<Trip>.removeTrip (element: Trip): Boolean {
    this.remove(element)
    this.reindex()
    return true
}

private fun MutableList<Trip>.reindex() {
    var count = 1
    for (task in this) {
//        task.index = count++
    }
}

enum class DocumentTypes {Hotel, Flight, Bus, Other}

data class TripDocument(
    val documentId: UUID,
    val eventId: UUID?,
    val fileName: String,
    val type: DocumentTypes,
    val addedBy: String,
    val extension: String,
    val mimeType: String,
)

data class FSTripDocument(
    val eventId: String = "",
    val fileName: String = "",
    val type: String = "Other",
    val addedBy: String = "",
    val extension: String = "",
    val mimeType: String = "*/*"
)

fun TripDocument.toFSTripDocument() = FSTripDocument(
    eventId = eventId?.toString() ?: "",
    fileName = fileName,
    type = type.name,
    addedBy = addedBy,
    extension = extension,
    mimeType = mimeType,
)

fun FSTripDocument.toTripDocument(documentId: UUID) = TripDocument(
    documentId = documentId,
    eventId = if (eventId == "") null else UUID.fromString(eventId),
    fileName = fileName,
    type = when(type) {
        "Hotel" -> DocumentTypes.Hotel
        "Flight" -> DocumentTypes.Flight
        "Bus" -> DocumentTypes.Bus
        else -> DocumentTypes.Other
    },
    addedBy = addedBy,
    extension = extension,
    mimeType = mimeType,
)
