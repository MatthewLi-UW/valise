package com.valise.mobile

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UpdateTrip(var tripIndex: Int = 0)

@Serializable
data class TripSummary(var tripIdAsString: String = "00000000-0000-0000-0000-000000000000")
// I dont really want to write a deserializer for UUID

@Serializable
object Home
@Serializable
object Login
@Serializable
object TripAdd

@Serializable
data class MapView(var tripIdAsString: String = "00000000-0000-0000-0000-000000000000",
                   var selectedEventIdAsString: String? = null)
