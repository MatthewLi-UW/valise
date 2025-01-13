package com.valise.mobile.view;

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.valise.mobile.entities.Trip
import com.valise.mobile.entities.TripEvent
import com.valise.mobile.model.ISubscriber
import com.valise.mobile.model.Model
import kotlinx.datetime.LocalDate
import java.util.UUID

class MapViewModel(private val model: Model) : ISubscriber {
    val list = mutableStateListOf<Trip>()

    init {
        model.subscribe(this)
    }

    override fun update() {
        val newList = model.list
        list.clear()
        list.addAll(newList)
    }

    fun getEventsForTrip(tripId: UUID): List<TripEvent> =
        list.firstOrNull { it.tripId == tripId }?.eventList ?: emptyList()

    fun getTripName(tripId: UUID): String =
        list.firstOrNull { it.tripId == tripId }?.destination ?: ""

    fun getDateRange(tripId: UUID): List<LocalDate> {
        val trip = list.firstOrNull { it.tripId == tripId }
        return if (trip != null) {
            val startDate = convertTimestampToLocalDateInUTC(trip.startTime ?: 0)
            val endDate = convertTimestampToLocalDateInUTC(trip.endTime ?: 0)
            dateRange(startDate, endDate)
        } else {
            emptyList()
        }
    }
}