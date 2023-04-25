package com.badoo.reaktive.completable

import com.badoo.reaktive.base.exceptions.TimeoutException
import com.badoo.reaktive.test.base.assertError
import com.badoo.reaktive.test.base.assertNotError
import com.badoo.reaktive.test.base.hasSubscribers
import com.badoo.reaktive.test.completable.TestCompletable
import com.badoo.reaktive.test.completable.assertComplete
import com.badoo.reaktive.test.completable.test
import com.badoo.reaktive.test.scheduler.TestScheduler
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class TimeoutTest : CompletableToCompletableTests by CompletableToCompletableTestsImpl({ timeout(1000L, TestScheduler()) }) {

    private val upstream = TestCompletable()
    private val other = TestCompletable()
    private val scheduler = TestScheduler()

    @Test
    fun completes_WHEN_other_completed() {
        val observer = upstream.timeout(1000L, scheduler, other).test()

        scheduler.timer.advanceBy(1.seconds)
        other.onComplete()

        observer.assertComplete()
    }

    @Test
    fun produces_error_WHEN_other_produced_error() {
        val observer = upstream.timeout(1000L, scheduler, other).test()
        val error = Exception()

        scheduler.timer.advanceBy(1.seconds)
        other.onError(error)

        observer.assertError(error)
    }

    @Test
    fun does_not_produce_error_WHEN_timeout_not_reached_after_subscribe() {
        val observer = upstream.timeout(1000L, scheduler).test()

        scheduler.timer.advanceBy(999.milliseconds)

        observer.assertNotError()
    }

    @Test
    fun produces_error_WHEN_timeout_reached_after_subscribe() {
        val observer = upstream.timeout(1000L, scheduler).test()

        scheduler.timer.advanceBy(1.seconds)

        observer.assertError { it is TimeoutException }
    }

    @Test
    fun does_not_subscribe_to_other_WHEN_timeout_not_reached_after_subscribe() {
        upstream.timeout(1000L, scheduler, other).test()

        scheduler.timer.advanceBy(999.seconds)

        assertFalse(other.hasSubscribers)
    }

    @Test
    fun subscribes_to_other_WHEN_timeout_reached_after_subscribe() {
        upstream.timeout(1000L, scheduler, other).test()

        scheduler.timer.advanceBy(1.seconds)

        assertTrue(other.hasSubscribers)
    }

    @Test
    fun does_not_produce_error_WHEN_timeout_reached_after_subscribe_and_has_other() {
        val observer = upstream.timeout(1000L, scheduler, other).test()

        scheduler.timer.advanceBy(1.seconds)

        observer.assertNotError()
    }
}
