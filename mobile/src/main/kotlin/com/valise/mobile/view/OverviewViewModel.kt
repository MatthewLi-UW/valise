package com.valise.mobile.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.valise.mobile.entities.Trip
import com.valise.mobile.model.ISubscriber
import com.valise.mobile.model.Model
import kotlinx.coroutines.Dispatchers.shutdown
import java.io.File
import java.io.IOException

class OverviewViewModel( val model: Model) : ISubscriber {
    val list = mutableStateListOf<Trip>()
    init {
        model.subscribe(this)
    }

    override fun update() {
        list.clear()
        for (task in model.list) {
            list.add(task)
        }
    }

    fun deleteTrip(tripToDelete: Trip) {
        model.del(tripToDelete)
    }


    // cover image feature
    companion object {
        private val tripImagesMap =
            mutableStateMapOf<String, Bitmap?>()  // Map of placeId to Bitmap
        val tripImages: Map<String, Bitmap?> = tripImagesMap

        fun fetchAndSetPhotoForTrip(context: Context, tripId: String, placeId: String) {
            if (tripImagesMap.containsKey(tripId)) return  // Skip if image is already cached

            val placesClient = Places.createClient(context)
            val fields = listOf(Place.Field.PHOTO_METADATAS)
            Log.d("OverviewViewModel","PlaceID check 1 NEW $placeId")
            val placeRequest = FetchPlaceRequest.newInstance(placeId, fields)
            Log.d("OverviewViewModel","PlaceID check 2 NEW $placeId")
            placesClient.fetchPlace(placeRequest)
                .addOnSuccessListener { response ->
                    val photoMetadata = response.place.photoMetadatas?.firstOrNull()
                    if (photoMetadata != null) {
                        val photoRequest = FetchPhotoRequest.builder(photoMetadata)
//                            .setMaxWidth(500)
//                            .setMaxHeight(300)
                            .build()

                        placesClient.fetchPhoto(photoRequest)
                            .addOnSuccessListener { fetchPhotoResponse ->
                                tripImagesMap[tripId] = fetchPhotoResponse.bitmap
                            }
                            .addOnFailureListener {
                                Log.e("OverviewViewModel", "Photo fetch failed 1", it)
                            }
                        Log.e("OverviewViewModel", "this part runs")
                    }
                }
                .addOnFailureListener {
                    Log.e("OverviewViewModel", "Place fetch failed 2", it)
                }
        }
    }
}