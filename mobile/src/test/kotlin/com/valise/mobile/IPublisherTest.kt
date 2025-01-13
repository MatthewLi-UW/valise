package com.valise.mobile

import com.valise.mobile.model.IPublisher
import com.valise.mobile.model.ISubscriber
import org.junit.Assert.assertFalse
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import java.util.UUID

// mock subscriber for testing
class MockSubscriber : ISubscriber {
    var isUpdated = false
    override fun update() {
        isUpdated = true
    }
}

// publisher for testing
class TestPublisher : IPublisher()

internal class IPublisherTest {
    @Test
    fun `test notify`() {
        val publisher = TestPublisher()
        val subscriber = MockSubscriber()

        publisher.subscribe(subscriber)

        // test if subscriber is updated
        assertTrue(subscriber.isUpdated)

        // reset and notify
        subscriber.isUpdated = false
        publisher.notifySubscribers()
        assertTrue(subscriber.isUpdated)
    }

    @Test
    fun `test add subscriber`() {
        val publisher = TestPublisher()
        val subscriber = MockSubscriber()

        // subscribe
        publisher.subscribe(subscriber)
        subscriber.isUpdated = false

        // notify
        publisher.notifySubscribers()

        // test if subscriber is notified
        assertTrue(subscriber.isUpdated)
    }

    @Test
    fun `test remove subscriber`() {
        val publisher = TestPublisher()
        val subscriber = MockSubscriber()

        // subscribe
        publisher.subscribe(subscriber)

        // unsubscribe
        publisher.unsubscribe(subscriber)

        // reset and notify
        subscriber.isUpdated = false
        publisher.notifySubscribers()

        // test if subscriber is not notified after unsubscribed
        assertFalse(subscriber.isUpdated)
    }

    @Test
    fun `test multiple subscribers`() {
        val publisher = TestPublisher()
        val subscriber1 = MockSubscriber()
        val subscriber2 = MockSubscriber()

        // subscribe both
        publisher.subscribe(subscriber1)
        publisher.subscribe(subscriber2)

        // reset state
        subscriber1.isUpdated = false
        subscriber2.isUpdated = false

        // notify subscribers
        publisher.notifySubscribers()

        // test if both subscribers are notified
        assertTrue(subscriber1.isUpdated)
        assertTrue(subscriber2.isUpdated)
    }

    @Test
    fun `test unsubscribed side effects`() {
        val publisher = TestPublisher()
        val subscriber1 = MockSubscriber()
        val subscriber2 = MockSubscriber()

        // subscribe
        publisher.subscribe(subscriber1)
        publisher.subscribe(subscriber2)

        // unsubscribe one
        publisher.unsubscribe(subscriber1)

        // reset states
        subscriber1.isUpdated = false
        subscriber2.isUpdated = false

        // notify
        publisher.notifySubscribers()

        // test that only subscriber2 is updated
        assertFalse(subscriber1.isUpdated)
        assertTrue(subscriber2.isUpdated)
    }
}