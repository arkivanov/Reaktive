package com.badoo.reaktive.observable

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.test.observable.DefaultObservableObserver
import com.badoo.reaktive.test.observable.TestObservable
import com.badoo.reaktive.test.observable.test
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
    ObservableToObservableTests by ObservableToObservableTestsImpl(
        transform = { observableUsing(resourceSupplier = {}, resourceCleanup = {}, sourceSupplier = { this }) }
    ),
    ObservableToObservableForwardTests by ObservableToObservableForwardTestsImpl(
        transform = { observableUsing(resourceSupplier = {}, resourceCleanup = {}, sourceSupplier = { this }) }
    ) {

    @Test
    fun acquires_new_resource_each_time_WHEN_eager_is_true_and_subscribed_multiple_times() {
        val pool = List(3) { Disposable() }
        var index by AtomicInt()
        val acquiredResources = AtomicList<Disposable>(emptyList())

        val downstream =
            observableUsing(resourceSupplier = { pool[index++] }, eager = true) {
                acquiredResources += it
                TestObservable()
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
            observableUsing(resourceSupplier = { pool[index++] }, eager = false) {
                acquiredResources += it
                TestObservable()
            }

        repeat(pool.size) { downstream.test() }

        assertContentEquals(pool, acquiredResources.value)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_true_and_downstream_disposed() {
        val resource = Disposable()
        val observer = observableUsing(resourceSupplier = { resource }, eager = true).test()

        observer.dispose()

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_true_and_upstream_completed() {
        val resource = Disposable()
        val upstream = TestObservable<Int>()
        observableUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream }).test()

        upstream.onComplete()

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_true_and_upstream_produced_error() {
        val resource = Disposable()
        val upstream = TestObservable<Int>()
        observableUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream }).test()

        upstream.onError(Exception())

        assertTrue(resource.isDisposed)
    }

    @Test
    fun resource_is_disposed_WHEN_eager_is_true_and_upstream_disposed() {
        var isResourceDisposedBeforeInnerObservable by AtomicBoolean()

        val observer =
            observableUsing(eager = true) { resource ->
                observableUnsafe<Nothing> { observer ->
                    observer.onSubscribe(
                        Disposable {
                            isResourceDisposedBeforeInnerObservable = resource.isDisposed
                        }
                    )
                }
            }.test()

        observer.dispose()

        assertTrue(isResourceDisposedBeforeInnerObservable)
    }

    @Test
    fun resource_is_disposed_WHEN_eager_is_true_and_downstream_received_onComplete() {
        val resource = Disposable()
        val upstream = TestObservable<Int>()
        var isResourceDisposedBeforeInnerObservable by AtomicBoolean()

        val downstream = observableUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultObservableObserver<Int> {
                override fun onComplete() {
                    isResourceDisposedBeforeInnerObservable = resource.isDisposed
                }
            }
        )

        upstream.onComplete()

        assertTrue(isResourceDisposedBeforeInnerObservable)
    }

    @Test
    fun resource_is_disposed_WHEN_eager_is_true_and_downstream_received_onError() {
        val resource = Disposable()
        val upstream = TestObservable<Int>()
        var isResourceDisposedBeforeInnerObservable by AtomicBoolean()

        val downstream = observableUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultObservableObserver<Int> {
                override fun onError(error: Throwable) {
                    isResourceDisposedBeforeInnerObservable = resource.isDisposed
                }
            }
        )

        upstream.onError(Exception())

        assertTrue(isResourceDisposedBeforeInnerObservable)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_false_and_downstream_disposed() {
        val resource = Disposable()
        val observer = observableUsing(resourceSupplier = { resource }, eager = true).test()

        observer.dispose()

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_false_and_upstream_completed() {
        val resource = Disposable()
        val upstream = TestObservable<Int>()
        observableUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream }).test()

        upstream.onComplete()

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_false_and_upstream_produced_error() {
        val resource = Disposable()
        val upstream = TestObservable<Int>()
        observableUsing(resourceSupplier = { resource }, eager = false, sourceSupplier = { upstream }).test()

        upstream.onError(Exception())

        assertTrue(resource.isDisposed)
    }

    @Test
    fun resource_is_not_disposed_WHEN_eager_is_false_and_upstream_disposed() {
        var isResourceDisposedBeforeInnerObservable by AtomicBoolean()

        val observer =
            observableUsing(eager = false) { resource ->
                observableUnsafe<Nothing> { observer ->
                    observer.onSubscribe(
                        Disposable {
                            isResourceDisposedBeforeInnerObservable = resource.isDisposed
                        }
                    )
                }
            }.test()

        observer.dispose()

        assertFalse(isResourceDisposedBeforeInnerObservable)
    }

    @Test
    fun resource_is_not_disposed_WHEN_eager_is_false_and_downstream_received_onComplete() {
        val resource = Disposable()
        val upstream = TestObservable<Int>()
        var isResourceDisposedBeforeInnerObservable by AtomicBoolean()

        val downstream = observableUsing(resourceSupplier = { resource }, eager = false, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultObservableObserver<Int> {
                override fun onComplete() {
                    isResourceDisposedBeforeInnerObservable = resource.isDisposed
                }
            }
        )

        upstream.onComplete()

        assertFalse(isResourceDisposedBeforeInnerObservable)
    }

    @Test
    fun resource_is_not_disposed_WHEN_eager_is_false_and_downstream_received_onError() {
        val resource = Disposable()
        val upstream = TestObservable<Int>()
        var isResourceDisposedBeforeInnerObservable by AtomicBoolean()

        val downstream = observableUsing(resourceSupplier = { resource }, eager = false, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultObservableObserver<Int> {
                override fun onError(error: Throwable) {
                    isResourceDisposedBeforeInnerObservable = resource.isDisposed
                }
            }
        )

        upstream.onError(Exception())

        assertFalse(isResourceDisposedBeforeInnerObservable)
    }

    private fun observableUsing(
        resourceSupplier: () -> Disposable = ::Disposable,
        eager: Boolean,
        sourceSupplier: (resource: Disposable) -> Observable<Int> = { TestObservable() },
    ): Observable<Int> =
        observableUsing(
            resourceSupplier = resourceSupplier,
            resourceCleanup = Disposable::dispose,
            eager = eager,
            sourceSupplier = sourceSupplier,
        )
}
