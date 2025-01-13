package com.valise.mobile.view

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.valise.mobile.entities.Trip
import com.valise.mobile.entities.TripEvent
import com.valise.mobile.model.ISubscriber
import com.valise.mobile.model.Model
import kotlinx.datetime.LocalDate
import java.io.File
import java.util.UUID
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.core.content.FileProvider
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class SummaryViewModel(private val model: Model) : ISubscriber {
    val list = mutableStateListOf<Trip>()

    init {
        model.subscribe(this)
    }

    override fun update() {
        list.clear()
        for (trip in model.list) {
            list.add(trip)
        }
    }

    fun getEventsByDate(tripId: UUID): Map<LocalDate, List<TripEvent>> =
        list.filter{it.tripId == tripId}.firstOrNull()?.eventList?.groupBy { it.startTime.date } ?: mapOf()

    fun getLocationStringByDate(tripId: UUID, date: LocalDate): String =
        list.filter{it.tripId == tripId}
            .firstOrNull()
            ?.cities
            ?.filter{it.startTime != null && it.endTime != null}
            ?.firstOrNull { date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds() in it.startTime!!..it.endTime!! }
            ?.name ?: "No dates set"

    fun onFileOpenRequest(
        context: Context,
        openFileLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
        url: String,
        fileName: String,
        mimeType: String
    ) {
        var fileRef = Firebase.storage.reference.child(url)
        val localFile = File(context.filesDir, fileName)
        fileRef.getFile(localFile)
            .addOnSuccessListener {
                val fileUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    localFile
                )

                val openFileIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(fileUri, mimeType)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                openFileLauncher.launch(openFileIntent)
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseStorage", "Error downloading file", exception)
            }
    }
}
