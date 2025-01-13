package com.valise.mobile

import com.google.firebase.Timestamp
import com.valise.mobile.entities.City
import com.valise.mobile.entities.DocumentTypes
import com.valise.mobile.entities.FSTrip
import com.valise.mobile.entities.FSTripDocument
import com.valise.mobile.entities.Trip
import com.valise.mobile.entities.TripDocument
import com.valise.mobile.entities.TripEvent
import com.valise.mobile.entities.addTrip
import com.valise.mobile.entities.removeTrip
import com.valise.mobile.entities.toFSTrip
import com.valise.mobile.entities.toFSTripDocument
import com.valise.mobile.entities.toTrip
import com.valise.mobile.entities.toTripDocument
import com.valise.mobile.entities.updateTrip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

internal class TripTest {
    @Test
    fun `test Trip initialization`() {
        val tripId = UUID.fromString("ab30952a-86eb-46bd-ada8-e4d860c7595f")
        val cities = mutableListOf<City>()
        val documents = mutableListOf<TripDocument>()
        val events = emptyList<TripEvent>().toMutableList()

        val trip = com.valise.mobile.entities.Trip(
            tripId = tripId,
            destination = "Kyoto",
            startTime = 1822512800000,
            endTime = 1822599200000,
            description = "A fun trip to Kyoto",
            cities = cities,
            backgroundImage = null,
            backgroundImageUrl = "",
            backgroundImageBitmap = null,
            documentList = documents,
            eventList = events,
            lastUpdatedAt = 1822600000000
        )

        // property initialization
        assertEquals(tripId, trip.tripId)
        assertEquals("Kyoto", trip.destination)
        assertEquals(1822512800000, trip.startTime)
        assertEquals(1822599200000, trip.endTime)
        assertEquals("A fun trip to Kyoto", trip.description)
        assertEquals(cities, trip.cities)
        assertEquals(documents, trip.documentList)
        assertEquals(events, trip.eventList)
        assertEquals("", trip.backgroundImageUrl)
        assertEquals(1822600000000, trip.lastUpdatedAt)

        // copy functionality
        val updatedTrip = trip.copy(destination = "Osaka")
        assertEquals("Osaka", updatedTrip.destination)
        assertEquals("Kyoto", trip.destination)

        // equals
        val identicalTrip = trip.copy()
        assertEquals(trip, identicalTrip)
        assertEquals(trip.hashCode(), identicalTrip.hashCode())
    }

    @Test
    fun `test FSTrip initialization`() {
        val fsTrip = FSTrip(
            destination = "Kyoto",
            startTime = Timestamp(1822512800, 0),
            endTime = Timestamp(1822599200, 0),
            description = "A fun trip to Kyoto",
            isActive = true,
            lastUpdatedAt = 1822600000000
        )

        // properties
        assertEquals("Kyoto", fsTrip.destination)
        assertEquals(Timestamp(1822512800, 0), fsTrip.startTime)
        assertEquals(Timestamp(1822599200, 0), fsTrip.endTime)
        assertEquals("A fun trip to Kyoto", fsTrip.description)
        assertTrue(fsTrip.isActive)
        assertEquals(1822600000000, fsTrip.lastUpdatedAt)

        // copy functionality
        val updatedFsTrip = fsTrip.copy(isActive = false)
        assertFalse(updatedFsTrip.isActive)
        assertTrue(fsTrip.isActive)

        // equals
        val identicalFsTrip = fsTrip.copy()
        assertEquals(fsTrip, identicalFsTrip)
        assertEquals(fsTrip.hashCode(), identicalFsTrip.hashCode())
    }

    @Test
    fun `test TripDocument initialization`() {
        val documentId = UUID.fromString("fa01d78c-059d-43c0-9704-4fea01576cb8")
        val tripDocument = TripDocument(
            documentId = documentId,
            eventId = null,
            fileName = "Painting.pdf",
            type = DocumentTypes.Hotel,
            addedBy = "Bob Ross",
            extension = "pdf",
            mimeType = "application/pdf"
        )

        // Test property initialization
        assertEquals(documentId, tripDocument.documentId)
        assertNull(tripDocument.eventId)
        assertEquals("Painting.pdf", tripDocument.fileName)
        assertEquals(DocumentTypes.Hotel, tripDocument.type)
        assertEquals("Bob Ross", tripDocument.addedBy)
        assertEquals("pdf", tripDocument.extension)
        assertEquals("application/pdf", tripDocument.mimeType)

        // copy
        val updatedDocument = tripDocument.copy(fileName = "CopiedPainting.pdf")
        assertEquals("CopiedPainting.pdf", updatedDocument.fileName)
        assertEquals("Painting.pdf", tripDocument.fileName) // Ensure original is unchanged

        // equals
        val identicalDocument = tripDocument.copy()
        assertEquals(tripDocument, identicalDocument)
        assertEquals(tripDocument.hashCode(), identicalDocument.hashCode())
    }

    @Test
    fun `test FSTripDocument initialization`() {
        val fsTripDocument = FSTripDocument(
            eventId = "fa01d78c-059d-43c0-9704-4fea01576cb8",
            fileName = "Ticket.pdf",
            type = "Flight",
            addedBy = "Bob the builder",
            extension = "pdf",
            mimeType = "application/pdf"
        )

        // properties
        assertEquals("fa01d78c-059d-43c0-9704-4fea01576cb8", fsTripDocument.eventId)
        assertEquals("Ticket.pdf", fsTripDocument.fileName)
        assertEquals("Flight", fsTripDocument.type)
        assertEquals("Bob the builder", fsTripDocument.addedBy)
        assertEquals("pdf", fsTripDocument.extension)
        assertEquals("application/pdf", fsTripDocument.mimeType)

        // copy
        val updatedFsDocument = fsTripDocument.copy(type = "Hotel")
        assertEquals("Hotel", updatedFsDocument.type)
        assertEquals("Flight", fsTripDocument.type)

        // equals
        val identicalFsDocument = fsTripDocument.copy()
        assertEquals(fsTripDocument, identicalFsDocument)
        assertEquals(fsTripDocument.hashCode(), identicalFsDocument.hashCode())
    }

    @Test
    fun `convert Trip to FSTrip`() {
        val trip = Trip(
            tripId = UUID.fromString("ab30952a-86eb-46bd-ada8-e4d860c7595f"),
            destination = "Kyoto",
            startTime = 1822512800000,
            endTime = 1822599200000,
            description = "A fun trip to Kyoto",
            cities = mutableListOf(),
            backgroundImage = null,
            backgroundImageUrl = null,
            backgroundImageBitmap = null,
            documentList = mutableListOf(),
            eventList = emptyList<TripEvent>().toMutableList(),
            lastUpdatedAt = 1822600000000
        )

        val fsTrip = trip.toFSTrip()

        assertEquals("Kyoto", fsTrip.destination)
        assertEquals(Timestamp(1822512800, 0), fsTrip.startTime)
        assertEquals(Timestamp(1822599200, 0), fsTrip.endTime)
        assertEquals("A fun trip to Kyoto", fsTrip.description)
        assertEquals(1822600000000, fsTrip.lastUpdatedAt)
        assertTrue(fsTrip.isActive)
    }

    @Test
    fun `convert FSTrip to Trip`() {
        val fsTrip = FSTrip(
            destination = "Osaka",
            startTime = Timestamp(1822512800, 0),
            endTime = Timestamp(1822599200, 0),
            description = "A fun trip to Osaka",
            isActive = true,
            lastUpdatedAt = 1822600000000
        )
        val cities = mutableListOf<City>()
        val documents = mutableListOf<TripDocument>()
        val events = mutableListOf<TripEvent>()
        val tripId = UUID.fromString("c82ba16a-967a-4864-8bd5-612b5aece704")

        val trip = fsTrip.toTrip(tripId, cities, documents, events)

        assertEquals("Osaka", trip.destination)
        assertEquals(1822512800000, trip.startTime)
        assertEquals(1822599200000, trip.endTime)
        assertEquals("A fun trip to Osaka", trip.description)
        assertEquals(tripId, trip.tripId)
        assertEquals(1822600000000, trip.lastUpdatedAt)
    }

    @Test
    fun `add a Trip to the list`() {
        val tripList = mutableListOf<Trip>()
        val trip = Trip(
            tripId = UUID.fromString("af01d78c-059d-43c0-9704-4fea01576cb8"),
            destination = "Waterloo",
            startTime = null,
            endTime = null,
            description = "Not fun trip to Waterloo",
            cities = mutableListOf(),
            backgroundImage = null,
            backgroundImageUrl = null,
            backgroundImageBitmap = null,
            documentList = mutableListOf(),
            eventList = emptyList<TripEvent>().toMutableList(),
            lastUpdatedAt = 0
        )

        tripList.addTrip(trip)

        assertEquals(1, tripList.size)
        assertEquals(trip, tripList[0])
    }

    @Test
    fun `update a Trip in the list`() {
        val tripList = mutableListOf(
            Trip(
                tripId = UUID.fromString("ab30952a-86eb-46bd-ada8-e4d860c7595f"),
                destination = "Kyoto",
                startTime = null,
                endTime = null,
                description = "A fun trip to Kyoto",
                cities = mutableListOf(),
                backgroundImage = null,
                backgroundImageUrl = null,
                backgroundImageBitmap = null,
                documentList = mutableListOf(),
                eventList = emptyList<TripEvent>().toMutableList(),
                lastUpdatedAt = 0
            )
        )

        val updatedTrip = tripList[0].copy(description = "Updated Kyoto trip")
        tripList.updateTrip(updatedTrip)

        assertEquals(1, tripList.size)
        assertEquals("Updated Kyoto trip", tripList[0].description)
    }

    @Test
    fun `remove a Trip from the list`() {
        val trip = Trip(
            tripId = UUID.fromString("ab30952a-86eb-46bd-ada8-e4d860c7595f"),
            destination = "Kyoto",
            startTime = null,
            endTime = null,
            description = "Trip to Kyoto!!! But not for long",
            cities = mutableListOf(),
            backgroundImage = null,
            backgroundImageUrl = null,
            backgroundImageBitmap = null,
            documentList = mutableListOf(),
            eventList = emptyList<TripEvent>().toMutableList(),
            lastUpdatedAt = 0
        )
        val tripList = mutableListOf(trip)

        tripList.removeTrip(trip)

        assertTrue(tripList.isEmpty())
    }

    @Test
    fun `convert TripDocument to FSTripDocument`() {
        val tripDocument = TripDocument(
            documentId = UUID.fromString("fa01d78c-059d-43c0-9704-4fea01576cb8"),
            eventId = null,
            fileName = "Itinerary.pdf",
            type = DocumentTypes.Hotel,
            addedBy = "Matt",
            extension = "pdf",
            mimeType = "application/pdf"
        )

        val fsTripDocument = tripDocument.toFSTripDocument()

        assertEquals("Itinerary.pdf", fsTripDocument.fileName)
        assertEquals("Hotel", fsTripDocument.type)
        assertEquals("Matt", fsTripDocument.addedBy)
        assertEquals("pdf", fsTripDocument.extension)
        assertEquals("application/pdf", fsTripDocument.mimeType)
    }

    @Test
    fun `convert FSTripDocument to TripDocument`() {
        val fsTripDocument = FSTripDocument(
            eventId = "",
            fileName = "Itinerary.pdf",
            type = "Flight",
            addedBy = "Matt",
            extension = "pdf",
            mimeType = "application/pdf"
        )
        val documentId = UUID.fromString("fa01d78c-059d-43c0-9704-4fea01576cb8")

        val tripDocument = fsTripDocument.toTripDocument(documentId)

        assertEquals(documentId, tripDocument.documentId)
        assertEquals(null, tripDocument.eventId)
        assertEquals("Flight", tripDocument.type.name)
        assertEquals("Matt", tripDocument.addedBy)
        assertEquals("pdf", tripDocument.extension)
        assertEquals("application/pdf", tripDocument.mimeType)
    }
}