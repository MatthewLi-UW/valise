package com.valise.mobile.controller

import android.util.Log
import com.valise.mobile.entities.Trip
import com.valise.mobile.model.Model
import com.valise.mobile.view.OverviewControllerEvent

class OverviewController(val model: Model) {
    fun invoke(event: OverviewControllerEvent, obj: Any) {
        Log.e("OverviewController", "PlaceId check add trip runs here")
        @Suppress("UNCHECKED_CAST")
        when(event) {
            OverviewControllerEvent.AddTrip -> {
                Log.e("AddTripMenu", "out of bounds check in overviewcontroller $obj")
                val trip = obj as Trip
                Log.e("AddTripMenu", "out of bounds check trip check $trip")
                //Log.d("OverviewController", "Adding trip with placeId: ${trip.cities[0]}")
                model.add(trip)
            }
            OverviewControllerEvent.DeleteTrip -> model.del(obj as Trip)
            OverviewControllerEvent.Update -> model.update((obj as HashMap<*, *>)["oldTrip"] as Trip, obj["updatedTrip"] as Trip)
//            TripEvent.Update -> model.list[(obj as Trip).tripId].destination = obj.content
            OverviewControllerEvent.Share -> model.share(obj as Pair<Trip, String>)
            OverviewControllerEvent.Refresh -> model.refresh()
            OverviewControllerEvent.Logout -> model.logout()
        }
    }
}