package com.valise.mobile.entities

import android.net.Uri
import com.google.firebase.Timestamp
import java.util.UUID
import kotlin.String

data class City(
    val cityId: UUID,
    var name: String,
    var startTime: Long?,
    var endTime: Long?,
    var placeId: String,
)

data class FSCity(
    var name: String = "",
    var startTime: Timestamp? = null,
    var endTime: Timestamp? = null,
    var placeId: String = "",
)

fun City.toFSCity() = FSCity(
    name = this.name,
    startTime = if(startTime != null) Timestamp(startTime!!/1000, 0) else null,
    endTime = if(endTime != null) Timestamp(endTime!!/1000, 0) else null,
    placeId = placeId,
)

fun FSCity.toCity(cityId: UUID)  = City(
    cityId = cityId,
    name = this.name,
    startTime = this.startTime?.toDate()?.time,
    endTime = this.endTime?.toDate()?.time,
    placeId = placeId,
)
