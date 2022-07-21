package com.badoo.reaktive.maybe

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.test.maybe.DefaultMaybeObserver
import com.badoo.reaktive.test.maybe.TestMaybe
import com.badoo.reaktive.test.maybe.test
import com.badoo.reaktive.utils.atomic.AtomicBoolean
import com.badoo.reaktive.utils.atomic.AtomicInt
import com.badoo.reaktive.utils.atomic.AtomicList
import com.badoo.reaktive.utils.atomic.getValue
import com.badoo.reaktive.utils.atomic.plusAssign
import com.badoo.reaktive.utils.atomic.setValue
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UsingTest :
    MaybeToMaybeTests by MaybeToMaybeTestsImpl(
        transform = { maybeUsing(resourceSupplier = {}, resourceCleanup = {}, sourceSupplier = { this }) }
    ) {

    @Test
    fun acquires_new_resource_each_time_WHEN_eager_is_true_and_subscribed_multiple_times() {
        val pool = List(3) { Disposable() }
        var index by AtomicInt()
        val acquiredResources = AtomicList<Disposable>(emptyList())

        val downstream =
            maybeUsing(resourceSupplier = { pool[index++] }, eager = true) {
                acquiredResources += it
                TestMaybe()
            }

        repeat(pool.size) { downstream.test() }

        assertContentEquals(pool, acquiredResources.value)
    }

    @Test
    fun acquires_new_resource_each_time_WHEN_eager_is_false_and_subscribed_multiple_times() {
        val pool = List(3) { Disposable() }
        var index by AtomicInt()
        val acquiredResources = AtomicList<Disposable>(emptyList())

        val downstream =
            maybeUsing(resourceSupplier = { pool[index++] }, eager = false) {
                acquiredResources += it
                TestMaybe()
            }

        repeat(pool.size) { downstream.test() }

        assertContentEquals(pool, acquiredResources.value)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_true_and_downstream_disposed() {
        val resource = Disposable()
        val observer = maybeUsing(resourceSupplier = { resource }, eager = true).test()

        observer.dispose()

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_true_and_upstream_completed() {
        val resource = Disposable()
        val upstream = TestMaybe<Int>()
        maybeUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream }).test()

        upstream.onComplete()

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_true_and_upstream_succeeded() {
        val resource = Disposable()
        val upstream = TestMaybe<Int>()
        maybeUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream }).test()

        upstream.onSuccess(0)

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_true_and_upstream_produced_error() {
        val resource = Disposable()
        val upstream = TestMaybe<Int>()
        maybeUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream }).test()

        upstream.onError(Exception())

        assertTrue(resource.isDisposed)
    }

    @Test
    fun resource_is_disposed_WHEN_eager_is_true_and_upstream_disposed() {
        var isResourceDisposedBeforeInnerMaybe by AtomicBoolean()

        val observer =
            maybeUsing(eager = true) { resource ->
                maybeUnsafe<Nothing> { observer ->
                    observer.onSubscribe(
                        Disposable {
                            isResourceDisposedBeforeInnerMaybe = resource.isDisposed
                        }
                    )
                }
            }.test()

        observer.dispose()

        assertTrue(isResourceDisposedBeforeInnerMaybe)
    }

    @Test
    fun resource_is_disposed_WHEN_eager_is_true_and_downstream_received_onComplete() {
        val resource = Disposable()
        val upstream = TestMaybe<Int>()
        var isResourceDisposedBeforeInnerMaybe by AtomicBoolean()

        val downstream = maybeUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultMaybeObserver<Int> {
                override fun onComplete() {
                    isResourceDisposedBeforeInnerMaybe = resource.isDisposed
                }
            }
        )

        upstream.onComplete()

        assertTrue(isResourceDisposedBeforeInnerMaybe)
    }

    @Test
    fun resource_is_disposed_WHEN_eager_is_true_and_downstream_received_onSuccess() {
        val resource = Disposable()
        val upstream = TestMaybe<Int>()
        var isResourceDisposedBeforeInnerMaybe by AtomicBoolean()

        val downstream = maybeUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultMaybeObserver<Int> {
                override fun onSuccess(value: Int) {
                    isResourceDisposedBeforeInnerMaybe = resource.isDisposed
                }
            }
        )

        upstream.onSuccess(0)

        assertTrue(isResourceDisposedBeforeInnerMaybe)
    }

    @Test
    fun resource_is_disposed_WHEN_eager_is_true_and_downstream_received_onError() {
        val resource = Disposable()
        val upstream = TestMaybe<Int>()
        var isResourceDisposedBeforeInnerMaybe by AtomicBoolean()

        val downstream = maybeUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultMaybeObserver<Int> {
                override fun onError(error: Throwable) {
                    isResourceDisposedBeforeInnerMaybe = resource.isDisposed
                }
            }
        )

        upstream.onError(Exception())

        assertTrue(isResourceDisposedBeforeInnerMaybe)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_false_and_downstream_disposed() {
        val resource = Disposable()
        val observer = maybeUsing(resourceSupplier = { resource }, eager = true).test()

        observer.dispose()

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_false_and_upstream_completed() {
        val resource = Disposable()
        val upstream = TestMaybe<Int>()
        maybeUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream }).test()

        upstream.onComplete()

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_false_and_upstream_succeeded() {
        val resource = Disposable()
        val upstream = TestMaybe<Int>()
        maybeUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream }).test()

        upstream.onSuccess(0)

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_false_and_upstream_produced_error() {
        val resource = Disposable()
        val upstream = TestMaybe<Int>()
        maybeUsing(resourceSupplier = { resource }, eager = false, sourceSupplier = { upstream }).test()

        upstream.onError(Exception())

        assertTrue(resource.isDisposed)
    }

    @Test
    fun resource_is_not_disposed_WHEN_eager_is_false_and_upstream_disposed() {
        var isResourceDisposedBeforeInnerMaybe by AtomicBoolean()

        val observer =
            maybeUsing(eager = false) { resource ->
                maybeUnsafe<Nothing> { observer ->
                    observer.onSubscribe(
                        Disposable {
                            isResourceDisposedBeforeInnerMaybe = resource.isDisposed
                        }
                    )
                }
            }.test()

        observer.dispose()

        assertFalse(isResourceDisposedBeforeInnerMaybe)
    }

    @Test
    fun resource_is_not_disposed_WHEN_eager_is_false_and_downstream_received_onComplete() {
        val resource = Disposable()
        val upstream = TestMaybe<Int>()
        var isResourceDisposedBeforeInnerMaybe by AtomicBoolean()

        val downstream = maybeUsing(resourceSupplier = { resource }, eager = false, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultMaybeObserver<Int> {
                override fun onComplete() {
                    isResourceDisposedBeforeInnerMaybe = resource.isDisposed
                }
            }
        )

        upstream.onComplete()

        assertFalse(isResourceDisposedBeforeInnerMaybe)
    }

    @Test
    fun resource_is_not_disposed_WHEN_eager_is_false_and_downstream_received_onSuccess() {
        val resource = Disposable()
        val upstream = TestMaybe<Int>()
        var isResourceDisposedBeforeInnerMaybe by AtomicBoolean()

        val downstream = maybeUsing(resourceSupplier = { resource }, eager = false, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultMaybeObserver<Int> {
                override fun onSuccess(value: Int) {
                    isResourceDisposedBeforeInnerMaybe = resource.isDisposed
                }
            }
        )

        upstream.onSuccess(0)

        assertFalse(isResourceDisposedBeforeInnerMaybe)
    }

    @Test
    fun resource_is_not_disposed_WHEN_eager_is_false_and_downstream_received_onError() {
        val resource = Disposable()
        val upstream = TestMaybe<Int>()
        var isResourceDisposedBeforeInnerMaybe by AtomicBoolean()

        val downstream = maybeUsing(resourceSupplier = { resource }, eager = false, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultMaybeObserver<Int> {
                override fun onError(error: Throwable) {
                    isResourceDisposedBeforeInnerMaybe = resource.isDisposed
                }
            }
        )

        upstream.onError(Exception())

        assertFalse(isResourceDisposedBeforeInnerMaybe)
    }

    private fun maybeUsing(
        resourceSupplier: () -> Disposable = ::Disposable,
        eager: Boolean,
        sourceSupplier: (resource: Disposable) -> Maybe<Int> = { TestMaybe() },
    ): Maybe<Int> =
        maybeUsing(
            resourceSupplier = resourceSupplier,
            resourceCleanup = Disposable::dispose,
            eager = eager,
            sourceSupplier = sourceSupplier,
        )
}
