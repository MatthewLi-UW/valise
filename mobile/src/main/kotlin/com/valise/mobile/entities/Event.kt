package com.valise.mobile.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

data class TripEvent(
    var eventName: String,
    var eventId: UUID,
    var location: GeoPoint?,
    var locationStr: String,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime?,
    var description: String,
    var isTimed: Boolean,
)

data class FSTripEvent(
    var eventName: String = "",
    var location: GeoPoint? = null,
    var locationStr: String = "",
    var startTime: Timestamp = Timestamp(0, 0),
    var endTime: Timestamp? = null,
    var description: String = "",
    var isTimed: Boolean = false
)

fun TripEvent.toFSTripEvent(): FSTripEvent {
    val startInstant = startTime.toInstant(TimeZone.currentSystemDefault())
    val endInstant = endTime?.toInstant(TimeZone.currentSystemDefault())

    return FSTripEvent(
        eventName = eventName,
        location = location,
        locationStr = locationStr,
        startTime = Timestamp(startInstant.toJavaInstant()),
        endTime = if(endInstant != null) Timestamp(endInstant.toJavaInstant()) else null,
        description = description,
        isTimed = isTimed,
    )
}

fun FSTripEvent.toTripEvent(eventId: UUID) = TripEvent(
    eventName = eventName,
    eventId = eventId,
    location = location,
    locationStr = locationStr,
    startTime = startTime.toInstant().toKotlinInstant().toLocalDateTime(TimeZone.currentSystemDefault()),
    endTime = endTime?.toInstant()?.toKotlinInstant()?.toLocalDateTime(TimeZone.currentSystemDefault()),
    description = description,
    isTimed = isTimed,
)
