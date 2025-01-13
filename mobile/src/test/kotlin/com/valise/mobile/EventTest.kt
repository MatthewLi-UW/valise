package com.valise.mobile

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.valise.mobile.entities.FSTripEvent
import com.valise.mobile.entities.TripEvent
import com.valise.mobile.entities.toFSTripEvent
import com.valise.mobile.entities.toTripEvent
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import java.util.UUID

internal class EventTest {
    @Test
    fun `convert TripEvent to FSTripEvent`() {
        val eventId = UUID.fromString("045dfaed-1f2f-45ee-9a8e-b794068bca8a")
        val geoPoint = GeoPoint(35.6895, 139.6917)
        val startTime = LocalDateTime(2024, 11, 1, 10, 0)
        val endTime = LocalDateTime(2024, 11, 1, 12, 0)

        val tripEvent = TripEvent(
            eventName = "Visit Don Quijote",
            eventId = eventId,
            location = geoPoint,
            locationStr = "",
            startTime = startTime,
            endTime = endTime,
            description = "Shopping",
            isTimed = true
        )

        val fsTripEvent = tripEvent.toFSTripEvent()

        assertEquals(tripEvent.eventName, fsTripEvent.eventName)
        assertEquals(tripEvent.location, fsTripEvent.location)
        assertEquals(
            tripEvent.startTime.toInstant(TimeZone.currentSystemDefault()).epochSeconds,
            fsTripEvent.startTime.seconds
        )
        assertEquals(
            tripEvent.endTime?.toInstant(TimeZone.currentSystemDefault())?.epochSeconds,
            fsTripEvent.endTime?.seconds
        )
        assertEquals(tripEvent.description, fsTripEvent.description)
        assertEquals(tripEvent.isTimed, fsTripEvent.isTimed)
    }

    @Test
    fun `test FSTripEvent to TripEvent conversion`() {
        val eventId = UUID.fromString("045dfaed-1f2f-45ee-9a8e-b794068bca8a")
        val geoPoint = GeoPoint(35.6895, 139.6917)
        val startTime = Timestamp(1698855600, 0)
        val endTime = Timestamp(1698862800, 0)

        val fsTripEvent = FSTripEvent(
            eventName = "Visit Don Quijote",
            location = geoPoint,
            locationStr = "",
            startTime = startTime,
            endTime = endTime,
            description = "Shopping",
            isTimed = true
        )

        val tripEvent = fsTripEvent.toTripEvent(eventId)

        assertEquals(fsTripEvent.eventName, tripEvent.eventName)
        assertEquals(eventId, tripEvent.eventId)
        assertEquals(fsTripEvent.location, tripEvent.location)
        assertEquals(
            fsTripEvent.startTime.toDate().time,
            tripEvent.startTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        )
        assertEquals(
            fsTripEvent.endTime?.toDate()?.time,
            tripEvent.endTime?.toInstant(TimeZone.currentSystemDefault())?.toEpochMilliseconds()
        )
        assertEquals(fsTripEvent.description, tripEvent.description)
        assertEquals(fsTripEvent.isTimed, tripEvent.isTimed)
    }

    @Test
    fun `test TripEvent initialization`() {
        val eventId = UUID.fromString("045dfaed-1f2f-45ee-9a8e-b794068bca8a")
        val geoPoint = GeoPoint(35.6895, 139.6917)
        val startTime = LocalDateTime(2024, 11, 1, 10, 0)
        val endTime = LocalDateTime(2024, 11, 1, 12, 0)

        val tripEvent = TripEvent(
            eventName = "Visit Shibuya",
            eventId = eventId,
            location = geoPoint,
            locationStr = "",
            startTime = startTime,
            endTime = endTime,
            description = "Looking at the people",
            isTimed = true
        )

        // properties
        assertEquals("Visit Shibuya", tripEvent.eventName)
        assertEquals(eventId, tripEvent.eventId)
        assertEquals(geoPoint, tripEvent.location)
        assertEquals(startTime, tripEvent.startTime)
        assertEquals(endTime, tripEvent.endTime)
        assertEquals("Looking at the people", tripEvent.description)
        assertTrue(tripEvent.isTimed)

        // copy
        val updatedEvent = tripEvent.copy(eventName = "Visit Skytree")
        assertEquals("Visit Skytree", updatedEvent.eventName)
        assertEquals("Visit Shibuya", tripEvent.eventName) // Ensure original is unchanged

        // equals
        val identicalEvent = tripEvent.copy()
        assertEquals(tripEvent, identicalEvent)
        assertEquals(tripEvent.hashCode(), identicalEvent.hashCode())
    }

    @Test
    fun `test FSTripEvent data class functionality`() {
        val geoPoint = GeoPoint(35.6895, 139.6917)
        val startTime = Timestamp(1698855600, 0) // 2024-11-1T10:00:00Z
        val endTime = Timestamp(1698862800, 0) // 2024-11-1T12:00:00Z

        val fsTripEvent = FSTripEvent(
            eventName = "Visit Shibuya",
            location = geoPoint,
            locationStr = "",
            startTime = startTime,
            endTime = endTime,
            description = "Looking at the people",
            isTimed = true
        )

        // properties
        assertEquals("Visit Shibuya", fsTripEvent.eventName)
        assertEquals(geoPoint, fsTripEvent.location)
        assertEquals(startTime, fsTripEvent.startTime)
        assertEquals(endTime, fsTripEvent.endTime)
        assertEquals("Looking at the people", fsTripEvent.description)
        assertTrue(fsTripEvent.isTimed)

        // copy
        val updatedFsEvent = fsTripEvent.copy(eventName = "Visit Skytree")
        assertEquals("Visit Skytree", updatedFsEvent.eventName)
        assertEquals("Visit Shibuya", fsTripEvent.eventName) // Ensure original is unchanged

        // equals
        val identicalFsEvent = fsTripEvent.copy()
        assertEquals(fsTripEvent, identicalFsEvent)
        assertEquals(fsTripEvent.hashCode(), identicalFsEvent.hashCode())
    }
}