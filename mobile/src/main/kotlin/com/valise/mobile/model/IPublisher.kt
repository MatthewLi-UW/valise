package com.valise.mobile.model

import com.valise.mobile.entities.TripDocument
import java.util.UUID

abstract class IPublisher {
    private val subscribers = mutableListOf<ISubscriber>()
    fun notifySubscribers() {
        subscribers.forEach {
            it.update()
        }
    }

    fun subscribe(subscriber: ISubscriber) {
        subscribers.add(subscriber)
        subscriber.update()
    }

    fun unsubscribe(subscriber: ISubscriber) {
        subscribers.remove(subscriber)
    }

}