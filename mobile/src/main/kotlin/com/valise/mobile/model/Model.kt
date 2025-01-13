package com.valise.mobile.model

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.valise.mobile.BuildConfig
import com.valise.mobile.entities.City
import com.valise.mobile.entities.FSCity
import com.valise.mobile.entities.FSTrip
import com.valise.mobile.entities.FSTripDocument
import com.valise.mobile.entities.FSTripEvent
import com.valise.mobile.entities.Trip
import com.valise.mobile.entities.TripDocument
import com.valise.mobile.entities.TripEvent
import com.valise.mobile.entities.addTrip
import com.valise.mobile.entities.removeTrip
import com.valise.mobile.entities.toCity
import com.valise.mobile.entities.toFSCity
import com.valise.mobile.entities.toFSTrip
import com.valise.mobile.entities.toFSTripDocument
import com.valise.mobile.entities.toFSTripEvent
import com.valise.mobile.entities.toTrip
import com.valise.mobile.entities.toTripDocument
import com.valise.mobile.entities.toTripEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import java.util.Collections.synchronizedList
import java.util.UUID

class Model() : IPublisher() {
    val list = synchronizedList(mutableListOf<Trip>())
    val tripDocRefs = mutableListOf<DocumentReference>()
    var tripRegistration : ListenerRegistration? = null
    var userRegistration : ListenerRegistration? = null
    val tripListeners = mutableListOf<ListenerRegistration>()


    fun unregister() {
        tripRegistration?.remove()
    }

    //https://stackoverflow.com/questions/66891349/java-lang-illegalstateexception-when-using-state-in-android-jetpack-compose
    private val defaultScope = CoroutineScope(Dispatchers.Default)
    private val scope = CoroutineScope(Dispatchers.Main)
    init {
        Firebase.auth.addAuthStateListener(FirebaseAuth.AuthStateListener { firebaseAuth ->
            if(firebaseAuth.currentUser != null)
                registerUserListener()
        })
    }

    fun add(trip: Trip) {
        list.addTrip(trip)
        Log.d("MODEL", "TRIP ADDED: $trip")
        addTripToFirestore(trip)
        notifySubscribers()
    }

    fun changeEventDayOrder(trip: Trip, orderOfEventsInADay: List<UUID>) {
//        val fromIndex = trip.eventList.indexOfFirst { it.eventId == fromId }
//        val toIndex = trip.eventList.indexOfFirst { it.eventId == toId }
//        val fromEvent = trip.eventList.first { it.eventId == fromId }
//        val toEvent = trip.eventList.first { it.eventId == toId }

        var mutableOrdering = trip.eventList.map{it.eventId}.toMutableList()
        mutableOrdering -= (orderOfEventsInADay)
        mutableOrdering += orderOfEventsInADay
        updateEventOrderingFirestore(trip.tripId, mutableOrdering)
        notifySubscribers()
    }

    fun share(obj: Pair<Trip, String>) {
        val trip = obj.first
        val email = obj.second
        val db = Firebase.firestore
        val tripRef = db.collection("trips")
            .document(trip.tripId.toString())

        db.collection("users").whereEqualTo("email", email).limit(1).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("users").document(document.id)
                        .update("trips", FieldValue.arrayUnion(tripRef))
                        .addOnSuccessListener {
                            Log.d("Model", "Added user $email to ${trip.tripId}")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Model", "Error adding user $email to ${trip.tripId}")
                        }
                }
            }.addOnFailureListener{
                Log.e("Model", "Error getting user $email to add to ${trip.tripId}")
            }
    }

    fun updateEventOrderingFirestore(tripId: UUID, eventsOrdering: List<UUID>) {
        if(Firebase.auth.currentUser == null) return // not the "kotlin way" but more readable than a ?.let {}
        val db = Firebase.firestore
        val tripRef = db.collection("trips")
            .document(tripId.toString())

        val eventRef = tripRef
            .collection("events")

        db.runBatch { batch ->
            batch.update(tripRef, "lastUpdatedAt", System.currentTimeMillis()/1000 )
            eventsOrdering.forEach { eventId ->
                batch.update(eventRef.document("event-order"), "order", FieldValue.arrayRemove(eventId.toString()))
            }
            eventsOrdering.forEach { eventId ->
                batch.update(eventRef.document("event-order"), "order", FieldValue.arrayUnion(eventId.toString()))
            }
        }
    }

    fun deleteEventFromFirestore(tripId: UUID, event: TripEvent) {
        if(Firebase.auth.currentUser == null) return // not the "kotlin way" but more readable than a ?.let {}
        val db = Firebase.firestore
        val tripRef = db.collection("trips")
            .document(tripId.toString())

        val eventRef = tripRef
            .collection("events")

        db.runBatch { batch ->
            batch.update(tripRef, "lastUpdatedAt", System.currentTimeMillis()/1000 )

            batch.delete(eventRef.document(event.eventId.toString()))

            // kinda just adds garbage data, but also need it to make sure the event-order document exists before appending
            batch.set(eventRef.document("event-order"), "lastUpdatedAt" to System.currentTimeMillis()/1000,
                SetOptions.merge() )

            batch.update(eventRef.document("event-order"), "order", FieldValue.arrayRemove(event.eventId.toString()))
        }
    }

    fun addEventsToFirestore(tripId: UUID, events: List<TripEvent>) {
        if(Firebase.auth.currentUser == null) return // not the "kotlin way" but more readable than a ?.let {}
        val db = Firebase.firestore
        val tripRef = db.collection("trips")
            .document(tripId.toString())

        val eventRef = tripRef
            .collection("events")
//        eventRef.document("event-order").set("lastUpdatedAt" to System.currentTimeMillis()/1000 )

        db.runBatch { batch ->
            batch.update(tripRef, "lastUpdatedAt", System.currentTimeMillis()/1000 )
            events.forEach { event ->
                batch.set(
                    eventRef.document(event.eventId.toString()),
                    event.toFSTripEvent(), SetOptions.merge())
                // kinda just adds garbage data, but also need it to make sure the event-order document exists before appending
                batch.set(eventRef.document("event-order"), "lastUpdatedAt" to System.currentTimeMillis()/1000,
                    SetOptions.merge() )
                batch.update(eventRef.document("event-order"), "order", FieldValue.arrayUnion(event.eventId.toString()))
            }
        }
    }

    // DOES NOT UPDATE EVENTS AND DOCUMENTS
    fun updateTripAndCitiesToFirestore(trip: Trip, citiesToDelete: List<City> = emptyList()) {
        if(Firebase.auth.currentUser == null) return // not the "kotlin way" but more readable than a ?.let {}
        val db = Firebase.firestore
        val tripRef = db.collection("trips")
            .document(trip.tripId.toString())
        val userRef = db.collection("users").document(Firebase.auth.currentUser!!.uid) // see above comment
        db.runBatch { batch ->
            batch.set(tripRef, trip.toFSTrip() )

            // since deleting entire subcollections is not recommended
            citiesToDelete.forEach {city ->
                batch.delete(tripRef.collection("cities").document(city.cityId.toString()))
            }

            trip.cities.forEach { city ->
                batch.set(
                    tripRef.collection("cities").document(city.cityId.toString()),
                    city.toFSCity())
            }

            batch.update(userRef, "trips", FieldValue.arrayUnion(tripRef))
            batch.update(tripRef, "lastUpdatedAt", System.currentTimeMillis()/1000 )
        }
        if(trip.eventList.isNotEmpty()) {
            addEventsToFirestore(trip.tripId, trip.eventList)
        }
    }

    fun addTripToFirestore(trip: Trip) {
        if(Firebase.auth.currentUser == null) return // not the "kotlin way" but more readable than a ?.let {}
        val db = Firebase.firestore
        val tripRef = db.collection("trips")
            .document(trip.tripId.toString())
        val userRef = db.collection("users").document(Firebase.auth.currentUser!!.uid) // see above comment
        db.runBatch { batch ->
            batch.set(tripRef, trip.toFSTrip(), SetOptions.merge() )

            trip.cities.forEach { city ->
                batch.set(
                    tripRef.collection("cities").document(city.cityId.toString()),
                    city.toFSCity(), SetOptions.merge())
            }
            batch.update(userRef, "trips", FieldValue.arrayUnion(tripRef))

            batch.update(tripRef, "lastUpdatedAt", System.currentTimeMillis()/1000 )
        }
        if(trip.eventList.isNotEmpty()) {
            addEventsToFirestore(trip.tripId, trip.eventList)
        }
    }

    fun deleteTripFromFirestore(trip: Trip) {
        if(Firebase.auth.currentUser == null) return // not the "kotlin way" but more readable than a ?.let {}
        val db = Firebase.firestore
        val tripRef = db.collection("trips")
            .document(trip.tripId.toString())
        val userRef = db.collection("users").document(Firebase.auth.currentUser!!.uid) // see above comment

        db.runBatch { batch ->
//            batch.set(tripRef, trip.toFSTrip().copy(isActive = false) )
            batch.delete(tripRef)
            batch.update(userRef, "trips", FieldValue.arrayRemove(tripRef))
        }
    }

    fun del(trip: Trip) {
        list.removeTrip(trip)
        deleteTripFromFirestore(trip)
        notifySubscribers()
    }

    val mutex = Mutex()

    suspend fun susRefreshTrip(tripId: UUID) {
        if(Firebase.auth.currentUser == null) return // not the "kotlin way" but more readable than a ?.let {}
        Log.d("Model", "refreshing tripId: $tripId")
        val db = Firebase.firestore
        val tripRef = db.collection("trips")
            .document(tripId.toString())
        val trip = tripRef.get().await()

//        list.removeAll { it.tripId == tripId }

        if(trip.exists()) {
            var retCities = mutableListOf<City>()
            val cities = tripRef.collection("cities").get().await()
            // gets all documents in "cities" subcollection, converts them to
            // City object, add them to cities
            cities.forEach { c ->
                // accidentally manually added a space to the end of a document id i added by hand.
                // cant rename documents.
                retCities += c.toObject(FSCity::class.java)
                    .toCity(UUID.fromString(c.id.replace(" ", "")))
            }

            var retDocuments = mutableListOf<TripDocument>()

            val tripDocuments = tripRef.collection("documents").get().await()
            tripDocuments.forEach { c ->
                retDocuments += c.toObject(FSTripDocument::class.java).toTripDocument(UUID.fromString(c.id))
            }

            var retEvents = mutableListOf<TripEvent>()
            val tripEvents = tripRef.collection("events").orderBy("startTime").get().await()
            val eventOrder = (tripRef.collection("events").document("event-order").get().await().get("order") ?: emptyList<String>()) as List<String>

//            var eventOrder = emptyList<String>()
            tripEvents.forEach { e ->
                if(e.id!="event-order")
                    retEvents += e.toObject(FSTripEvent::class.java)
                        .toTripEvent(UUID.fromString(e.id))
            }

            val c = trip.toObject(FSTrip::class.java)

            val orderedEvents = eventOrder.map { eventId -> retEvents.find {it.eventId.toString() == eventId}}.filterNotNull().toMutableList()

            if (c != null && c.isActive) {
                list.removeAll { it.tripId == tripId }
                list.add(
                    c.toTrip(
                        UUID.fromString(trip.id),
                        retCities,
                        retDocuments,
                        orderedEvents
                    )
                )
                list.sortByDescending {it.lastUpdatedAt}
            }
            assert(mutex.isLocked)
            notifySubscribers()
        }
    }

    fun update(oldTrip: Trip, updatedTrip: Trip) {
        // Find and update the trip in the local list
        val index = list.indexOfFirst { it.tripId == updatedTrip.tripId }

        if (index != -1) {
            list[index] = updatedTrip
        } else {
            // If not found, add the trip
            list.addTrip(updatedTrip)
        }

        Log.d("MODEL", "TRIP UPDATED: $updatedTrip")

        val citiesToDelete = oldTrip.cities.subtract(updatedTrip.cities).toList()

        // Push the updated trip to Firestore
        updateTripAndCitiesToFirestore(updatedTrip, citiesToDelete)

        // Notify subscribers of the change
        notifySubscribers()
    }

    suspend fun susRefresh() {
        if(Firebase.auth.currentUser == null) return // not the "kotlin way" but more readable than a ?.let {}
        Log.d("Model", "refreshed")
        val db = Firebase.firestore

        val userRef = db.collection("users").document(Firebase.auth.currentUser!!.uid) // see above comment
        val user = userRef.get().await()
        if (user != null && user.data?.get("trips") != null) {
            list.clear()

            (user.data?.get("trips") as List<*>).forEach { tripRef ->
                val trip = (tripRef as DocumentReference).get().await()
                if(trip.exists()) {
                    var retCities = mutableListOf<City>()
                    val cities = tripRef.collection("cities").get().await()
                    // gets all documents in "cities" subcollection, converts them to
                    // City object, add them to cities
                    cities.forEach { c ->
                        // accidentally manually added a space to the end of a document id i added by hand.
                        // cant rename documents.
                        retCities += c.toObject(FSCity::class.java)
                            .toCity(UUID.fromString(c.id.replace(" ", "")))
                    }

//                    var storageRef = storage.reference
                    var retDocuments = mutableListOf<TripDocument>()

                    val tripDocuments = tripRef.collection("documents").get().await()
                    tripDocuments.forEach { c ->
                        retDocuments += c.toObject(FSTripDocument::class.java).toTripDocument(UUID.fromString(c.id))
                    }

                    var retEvents = mutableListOf<TripEvent>()
                    val tripEvents = tripRef.collection("events").get().await()
                    tripEvents.forEach { e ->
                        retEvents += e.toObject(FSTripEvent::class.java)
                            .toTripEvent(UUID.fromString(e.id))
                    }

                    val c = trip.toObject(FSTrip::class.java)

                    if (c != null && c.isActive)
                        list.addTrip(
                            c.toTrip(
                                UUID.fromString(trip.id),
                                retCities,
                                retDocuments,
                                retEvents
                            )
                        )

                }
            }
        }
        list.sortByDescending {it.lastUpdatedAt}
        notifySubscribers()
        defaultScope.launch {
            mutex.withLock {
                registerTripsListener()
            }
        }
    }

    fun refreshTrip(tripId: UUID) {
        scope.launch {
            mutex.withLock {
                susRefreshTrip(tripId)
            }
        }
    }

    fun refresh() {
        return
//        scope.launch {
//            susRefresh()
//        }
    }

    fun logout() {
        Firebase.auth.signOut()
        list.clear()
        notifySubscribers()
    }

    fun deleteDocument(tripId: UUID, documentId: UUID) {
        list.first{it.tripId == tripId}.documentList.removeAll{it.documentId == documentId}
        deleteDocumentFromFirestore(tripId = tripId, documentId = documentId)
    }

    fun addDocument(tripId: UUID, document: TripDocument, inputStream: InputStream) {
        list.first{it.tripId == tripId}.documentList += document
        addDocumentToFirestore(tripId = tripId, document = document)
        addDocumentToCloudStorage(document.documentId.toString() + '.' + document.extension, inputStream)
    }

    fun addDocumentToCloudStorage(fileName: String, stream: InputStream) {
        var uploadTask = Firebase.storage.reference.child(fileName).putStream(stream)
        uploadTask.addOnFailureListener {
            stream.close()
        }.addOnSuccessListener { taskSnapshot ->
            stream.close()
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }

    fun registerTripsListener() {
        if(Firebase.auth.currentUser == null) return // not the "kotlin way" but more readable than a ?.let {}
        val db = Firebase.firestore

        val userDocumentReference = db
            .collection("users")
            .document(Firebase.auth.currentUser!!.uid)

        userDocumentReference.get().addOnSuccessListener { doc ->
            if(!doc.exists()) Log.e("MODEL", "User document not found")
            val allowedTrips = doc["trips"] as? List<*> ?: emptyList<Any>()

            tripListeners.forEach { it.remove() }
            tripListeners.clear()

            for (tripRef in allowedTrips) {
                Log.d("MODEL", "Registered!")
                val listener = (tripRef as DocumentReference).addSnapshotListener { snapshot, e ->
                    if(e != null) {
                        Log.e("MODEL", "error listening to trip: ${e.message}")
                        return@addSnapshotListener
                    }

                    if(snapshot != null && snapshot.exists()) {
                        val tripId = snapshot.id
                        refreshTrip(tripId = UUID.fromString( tripId ))
                    }
                }
                tripListeners.add(listener)
            }
        }
    }



    /// MAKE SURE WE ARE SIGNED IN!!
    fun registerUserListener() {
        val db = Firebase.firestore
        val tripDocumentQuery = db
            .collection("users")
            .document(Firebase.auth.currentUser!!.uid)
        userRegistration?.remove()
        userRegistration = tripDocumentQuery.addSnapshotListener { value, e ->
            if (e != null) {
                Log.w("MODEL", "Listen failed.", e)
                return@addSnapshotListener
            }

            if(value == null || !value.exists()) {
                Log.w("MODEL", "getting data failed / nonexistant", e)
                return@addSnapshotListener
            }

            Log.d("MODEL", "Refreshing user's shared trips!")

            registerTripsListener()
        }
    }

    fun registerDocumentListener(tripId: UUID): ListenerRegistration {
        val db = Firebase.firestore
        val tripDocumentQuery = db
            .collection("trips")
            .document(tripId.toString())
            .collection("documents")

        val registration = tripDocumentQuery.addSnapshotListener { value, e ->
            if (e != null) {
                Log.w("MODEL", "Listen failed.", e)
                return@addSnapshotListener
            }
            Log.d("MODEL", "Registered!!")
            val retDocuments = mutableListOf<TripDocument>()
            for (doc in value!!) {
                retDocuments += doc.toObject(FSTripDocument::class.java).toTripDocument(UUID.fromString(doc.id))
            }
            list.first { it.tripId == tripId }.documentList = retDocuments
            notifySubscribers()
        }
        return registration
    }

    fun addDocumentToFirestore(tripId: UUID, document: TripDocument) {
        if(Firebase.auth.currentUser == null) return // not the "kotlin way" but more readable than a ?.let {}
        val db = Firebase.firestore

        val tripRef = db
            .collection("trips")
            .document(tripId.toString())
        val tripDocumentRef = db
            .collection("trips")
            .document(tripId.toString())
            .collection("documents")
            .document(document.documentId.toString())

        tripDocumentRef.set(document.toFSTripDocument(), SetOptions.merge())
        tripRef.update("lastUpdatedAt", System.currentTimeMillis()/1000 )
    }

    fun deleteDocumentFromFirestore(tripId: UUID, documentId: UUID) {
        if(Firebase.auth.currentUser == null) return // not the "kotlin way" but more readable than a ?.let {}
        val db = Firebase.firestore

        val tripRef = db
            .collection("trips")
            .document(tripId.toString())
        val tripDocumentRef = db
            .collection("trips")
            .document(tripId.toString())
            .collection("documents")
            .document(documentId.toString())

        tripDocumentRef.delete()
        tripRef.update("lastUpdatedAt", System.currentTimeMillis()/1000 )
    }

    fun addEvent(tripId: UUID, event: TripEvent) {
        list.first{it.tripId == tripId}.eventList += event
        addEventsToFirestore(tripId, listOf(event))
    }

    fun deleteEvent(tripId: UUID, event: TripEvent) {
        val index = list.indexOfFirst{it.tripId == tripId}
        val eventIndex = list[index].eventList.indexOfFirst { it.eventId == event.eventId }
        list[index].eventList.removeAt(eventIndex)
        deleteEventFromFirestore(tripId, event)
        deleteEventFromFirestore(tripId, event)
    }

    fun updateEvent(tripId: UUID, event: TripEvent) {
        val index = list.indexOfFirst{it.tripId == tripId}
        val eventIndex = list[index].eventList.indexOfFirst { it.eventId == event.eventId }
        list[index].eventList[eventIndex] = event
        addEventsToFirestore(tripId, listOf(event))
    }

    override fun toString(): String {
        var s = ""
        list.forEach { s += "[${it.destination}] ${it.description}\n" }
        return s
    }

    fun googleSignInSuccess(result: AuthResult, onFSSuccess: () -> Unit, onFSFailure: (Exception) -> Unit) {
        val db = Firebase.firestore
        val user = hashMapOf(
            "email" to result.user?.email
        )

        db.collection("users")
            .document(result.user?.uid ?: "null")
            .set(user, SetOptions.merge())
            .addOnSuccessListener { documentReference ->
//                scope.launch {
//                    susRefresh()
//                }
//                refresh()
                onFSSuccess()
            }
            .addOnFailureListener { e ->
                onFSFailure(e)
            }
    }


    fun googleSignIn(context: Context): Flow<Result<AuthResult>> {
        val firebaseAuth = Firebase.auth
        return callbackFlow {
            try {
                // Initialize Credential Manager
                val credentialManager: CredentialManager = CredentialManager.create(context)

                // Set up Google ID option
                val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(
                    serverClientId = BuildConfig.WEB_CLIENT_ID,

                    )
                    .build()

                val request: GetCredentialRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                    trySend(Result.success(authResult))
                } else {
                    throw RuntimeException("Received an invalid credential type")
                }
            } catch (e: GetCredentialCancellationException) {
                trySend(Result.failure(Exception("Sign-in was canceled. Please try again.", e)))

            } catch (e: Exception) {
                trySend(Result.failure(e))
            }
            awaitClose { }
        }
    }

    fun guestSignIn(): Flow<Result<AuthResult>> {
        val auth = Firebase.auth
        return callbackFlow {
            try {

                auth.signInAnonymously()
                    .addOnSuccessListener { task ->
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("MODEL", "signInAnonymously:success")
                        trySend(Result.success(task))
                    }
                    .addOnFailureListener{ e ->
                        trySend(Result.failure(Exception("Sign-in was canceled. Please try again.", e)))
                    }
            } catch (e: GetCredentialCancellationException) {
                trySend(Result.failure(Exception("Sign-in was canceled. Please try again.", e)))

            } catch (e: Exception) {
                trySend(Result.failure(e))
            }
            awaitClose {}
        }
    }
}