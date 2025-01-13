package com.valise.mobile.controller

import com.valise.mobile.entities.Trip
import com.valise.mobile.entities.TripDocument
import com.valise.mobile.entities.TripEvent
import com.valise.mobile.model.Model
import com.valise.mobile.view.TripSummaryControllerEvent
import java.io.InputStream
import java.util.HashMap
import java.util.UUID

@Suppress("UNCHECKED_CAST")
class SummaryController(val model: Model) {
    fun invoke(event: TripSummaryControllerEvent, obj: Any) : Any {
        return when(event) {
            TripSummaryControllerEvent.AddEvent -> model.addEvent((obj as HashMap<*, *>)["tripId"] as UUID, obj["event"] as TripEvent)
            TripSummaryControllerEvent.DeleteEvent -> model.deleteEvent((obj as HashMap<*, *>)["tripId"] as UUID, obj["event"] as TripEvent)
            TripSummaryControllerEvent.UpdateEvent -> model.updateEvent((obj as HashMap<*, *>)["tripId"] as UUID, obj["event"] as TripEvent)
            TripSummaryControllerEvent.AddDocument -> model.addDocument((obj as HashMap<*, *>)["tripId"] as UUID, obj["document"] as TripDocument, obj["inputStream"] as InputStream)
            TripSummaryControllerEvent.ShareTrip -> model.share(obj as Pair<Trip, String>)
            TripSummaryControllerEvent.DeleteDocument -> model.deleteDocument((obj as HashMap<*, *>)["tripId"] as UUID, obj["documentId"] as UUID)
            TripSummaryControllerEvent.SwapEvent -> model.changeEventDayOrder((obj as HashMap<*, *>)["trip"] as Trip, obj["orders"] as List<UUID>)

        }
    }
}