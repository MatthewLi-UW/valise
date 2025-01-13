package com.valise.mobile.controller

import com.valise.mobile.view.LoginViewEvent
import com.valise.mobile.model.Model

class LoginController(val model: Model) {
    fun invoke(event: LoginViewEvent, obj: Any) {
        when(event) {
            LoginViewEvent.Refresh -> model.refresh()
        }
    }
}