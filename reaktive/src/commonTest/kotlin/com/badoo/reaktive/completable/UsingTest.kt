package com.badoo.reaktive.completable

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.test.completable.DefaultCompletableObserver
import com.badoo.reaktive.test.completable.TestCompletable
import com.badoo.reaktive.test.completable.test
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
    CompletableToCompletableTests by CompletableToCompletableTestsImpl(
        transform = { completableUsing(resourceSupplier = {}, resourceCleanup = {}, sourceSupplier = { this }) }
    ) {

    @Test
    fun acquires_new_resource_each_time_WHEN_eager_is_true_and_subscribed_multiple_times() {
        val pool = List(3) { Disposable() }
        var index by AtomicInt()
        val acquiredResources = AtomicList<Disposable>(emptyList())

        val downstream =
            completableUsing(resourceSupplier = { pool[index++] }, eager = true) {
                acquiredResources += it
                TestCompletable()
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
            completableUsing(resourceSupplier = { pool[index++] }, eager = false) {
                acquiredResources += it
                TestCompletable()
            }

        repeat(pool.size) { downstream.test() }

        assertContentEquals(pool, acquiredResources.value)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_true_and_downstream_disposed() {
        val resource = Disposable()
        val observer = completableUsing(resourceSupplier = { resource }, eager = true).test()

        observer.dispose()

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_true_and_upstream_completed() {
        val resource = Disposable()
        val upstream = TestCompletable()
        completableUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream }).test()

        upstream.onComplete()

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_true_and_upstream_produced_error() {
        val resource = Disposable()
        val upstream = TestCompletable()
        completableUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream }).test()

        upstream.onError(Exception())

        assertTrue(resource.isDisposed)
    }

    @Test
    fun resource_is_disposed_WHEN_eager_is_true_and_upstream_disposed() {
        var isResourceDisposedBeforeInnerCompletable by AtomicBoolean()

        val observer =
            completableUsing(eager = true) { resource ->
                completableUnsafe { observer ->
                    observer.onSubscribe(
                        Disposable {
                            isResourceDisposedBeforeInnerCompletable = resource.isDisposed
                        }
                    )
                }
            }.test()

        observer.dispose()

        assertTrue(isResourceDisposedBeforeInnerCompletable)
    }

    @Test
    fun resource_is_disposed_WHEN_eager_is_true_and_downstream_received_onComplete() {
        val resource = Disposable()
        val upstream = TestCompletable()
        var isResourceDisposedBeforeInnerCompletable by AtomicBoolean()

        val downstream = completableUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultCompletableObserver {
                override fun onComplete() {
                    isResourceDisposedBeforeInnerCompletable = resource.isDisposed
                }
            }
        )

        upstream.onComplete()

        assertTrue(isResourceDisposedBeforeInnerCompletable)
    }

    @Test
    fun resource_is_disposed_WHEN_eager_is_true_and_downstream_received_onError() {
        val resource = Disposable()
        val upstream = TestCompletable()
        var isResourceDisposedBeforeInnerCompletable by AtomicBoolean()

        val downstream = completableUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultCompletableObserver {
                override fun onError(error: Throwable) {
                    isResourceDisposedBeforeInnerCompletable = resource.isDisposed
                }
            }
        )

        upstream.onError(Exception())

        assertTrue(isResourceDisposedBeforeInnerCompletable)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_false_and_downstream_disposed() {
        val resource = Disposable()
        val observer = completableUsing(resourceSupplier = { resource }, eager = true).test()

        observer.dispose()

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_false_and_upstream_completed() {
        val resource = Disposable()
        val upstream = TestCompletable()
        completableUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream }).test()

        upstream.onComplete()

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_false_and_upstream_produced_error() {
        val resource = Disposable()
        val upstream = TestCompletable()
        completableUsing(resourceSupplier = { resource }, eager = false, sourceSupplier = { upstream }).test()

        upstream.onError(Exception())

        assertTrue(resource.isDisposed)
    }

    @Test
    fun resource_is_not_disposed_WHEN_eager_is_false_and_upstream_disposed() {
        var isResourceDisposedBeforeInnerCompletable by AtomicBoolean()

        val observer =
            completableUsing(eager = false) { resource ->
                completableUnsafe { observer ->
                    observer.onSubscribe(
                        Disposable {
                            isResourceDisposedBeforeInnerCompletable = resource.isDisposed
                        }
                    )
                }
            }.test()

        observer.dispose()

        assertFalse(isResourceDisposedBeforeInnerCompletable)
    }

    @Test
    fun resource_is_not_disposed_WHEN_eager_is_false_and_downstream_received_onComplete() {
        val resource = Disposable()
        val upstream = TestCompletable()
        var isResourceDisposedBeforeInnerCompletable by AtomicBoolean()

        val downstream = completableUsing(resourceSupplier = { resource }, eager = false, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultCompletableObserver {
                override fun onComplete() {
                    isResourceDisposedBeforeInnerCompletable = resource.isDisposed
                }
            }
        )

        upstream.onComplete()

        assertFalse(isResourceDisposedBeforeInnerCompletable)
    }

    @Test
    fun resource_is_not_disposed_WHEN_eager_is_false_and_downstream_received_onError() {
        val resource = Disposable()
        val upstream = TestCompletable()
        var isResourceDisposedBeforeInnerCompletable by AtomicBoolean()

        val downstream = completableUsing(resourceSupplier = { resource }, eager = false, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultCompletableObserver {
                override fun onError(error: Throwable) {
                    isResourceDisposedBeforeInnerCompletable = resource.isDisposed
                }
            }
        )

        upstream.onError(Exception())

        assertFalse(isResourceDisposedBeforeInnerCompletable)
    }

    private fun completableUsing(
        resourceSupplier: () -> Disposable = ::Disposable,
        eager: Boolean,
        sourceSupplier: (resource: Disposable) -> Completable = { TestCompletable() },
    ): Completable =
        completableUsing(
            resourceSupplier = resourceSupplier,
            resourceCleanup = Disposable::dispose,
            eager = eager,
            sourceSupplier = sourceSupplier,
        )
}
