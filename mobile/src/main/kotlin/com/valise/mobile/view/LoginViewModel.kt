package com.valise.mobile.view

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import com.valise.mobile.entities.Trip
import com.valise.mobile.model.ISubscriber
import com.valise.mobile.model.Model
import kotlinx.coroutines.launch

class LoginViewModel(private val model: Model) : ISubscriber, ViewModel() {
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

    fun handleGuestSignIn(context: Context, onSuccess: (AuthResult) -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            model.guestSignIn().collect { result ->
                result.fold(
                    onSuccess = {
                        Log.d("FIREBASE", "success")
                        onSuccess(it)
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            context,
                            "Failed to sign in as guest.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        Log.e("LOGIN", "Failed to sign in as guest", e)
                        onFailure(e)
                    }
                )
            }
        }
    }

    fun handleGoogleSignIn(context: Context, onSuccess: (AuthResult) -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            model.googleSignIn(context).collect { result ->
                result.fold(
                    onSuccess = {
                        Log.d("FIREBASE", "success")
                        onSuccess(it)
                    },
                    onFailure = { e ->
                        Log.e("FIREBASE", "Failed to sign in with Google. Check your wifi connection.", e)
                        Toast.makeText(
                            context,
                            "Failed to sign in with Google. Check your wifi connection.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        onFailure(e)
                    }
                )
            }
        }
    }

    fun googleSignInSuccess(result: AuthResult, onFSSuccess: () -> Unit, onFSFailure: (Exception) -> Unit) {
        model.googleSignInSuccess(result, onFSSuccess, onFSFailure)
    }
}