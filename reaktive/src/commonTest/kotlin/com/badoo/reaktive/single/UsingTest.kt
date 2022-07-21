package com.badoo.reaktive.single

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.test.single.DefaultSingleObserver
import com.badoo.reaktive.test.single.TestSingle
import com.badoo.reaktive.test.single.test
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
    SingleToSingleTests by SingleToSingleTestsImpl(
        transform = { singleUsing(resourceSupplier = {}, resourceCleanup = {}, sourceSupplier = { this }) }
    ) {

    @Test
    fun acquires_new_resource_each_time_WHEN_eager_is_true_and_subscribed_multiple_times() {
        val pool = List(3) { Disposable() }
        var index by AtomicInt()
        val acquiredResources = AtomicList<Disposable>(emptyList())

        val downstream =
            singleUsing(resourceSupplier = { pool[index++] }, eager = true) {
                acquiredResources += it
                TestSingle()
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
            singleUsing(resourceSupplier = { pool[index++] }, eager = false) {
                acquiredResources += it
                TestSingle()
            }

        repeat(pool.size) { downstream.test() }

        assertContentEquals(pool, acquiredResources.value)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_true_and_downstream_disposed() {
        val resource = Disposable()
        val observer = singleUsing(resourceSupplier = { resource }, eager = true).test()

        observer.dispose()

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_true_and_upstream_succeeded() {
        val resource = Disposable()
        val upstream = TestSingle<Int>()
        singleUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream }).test()

        upstream.onSuccess(0)

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_true_and_upstream_produced_error() {
        val resource = Disposable()
        val upstream = TestSingle<Int>()
        singleUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream }).test()

        upstream.onError(Exception())

        assertTrue(resource.isDisposed)
    }

    @Test
    fun resource_is_disposed_WHEN_eager_is_true_and_upstream_disposed() {
        var isResourceDisposedBeforeInnerSingle by AtomicBoolean()

        val observer =
            singleUsing(eager = true) { resource ->
                singleUnsafe<Nothing> { observer ->
                    observer.onSubscribe(
                        Disposable {
                            isResourceDisposedBeforeInnerSingle = resource.isDisposed
                        }
                    )
                }
            }.test()

        observer.dispose()

        assertTrue(isResourceDisposedBeforeInnerSingle)
    }

    @Test
    fun resource_is_disposed_WHEN_eager_is_true_and_downstream_received_onSuccess() {
        val resource = Disposable()
        val upstream = TestSingle<Int>()
        var isResourceDisposedBeforeInnerSingle by AtomicBoolean()

        val downstream = singleUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultSingleObserver<Int> {
                override fun onSuccess(value: Int) {
                    isResourceDisposedBeforeInnerSingle = resource.isDisposed
                }
            }
        )

        upstream.onSuccess(0)

        assertTrue(isResourceDisposedBeforeInnerSingle)
    }

    @Test
    fun resource_is_disposed_WHEN_eager_is_true_and_downstream_received_onError() {
        val resource = Disposable()
        val upstream = TestSingle<Int>()
        var isResourceDisposedBeforeInnerSingle by AtomicBoolean()

        val downstream = singleUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultSingleObserver<Int> {
                override fun onError(error: Throwable) {
                    isResourceDisposedBeforeInnerSingle = resource.isDisposed
                }
            }
        )

        upstream.onError(Exception())

        assertTrue(isResourceDisposedBeforeInnerSingle)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_false_and_downstream_disposed() {
        val resource = Disposable()
        val observer = singleUsing(resourceSupplier = { resource }, eager = true).test()

        observer.dispose()

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_false_and_upstream_succeeded() {
        val resource = Disposable()
        val upstream = TestSingle<Int>()
        singleUsing(resourceSupplier = { resource }, eager = true, sourceSupplier = { upstream }).test()

        upstream.onSuccess(0)

        assertTrue(resource.isDisposed)
    }

    @Test
    fun disposes_resource_WHEN_eager_is_false_and_upstream_produced_error() {
        val resource = Disposable()
        val upstream = TestSingle<Int>()
        singleUsing(resourceSupplier = { resource }, eager = false, sourceSupplier = { upstream }).test()

        upstream.onError(Exception())

        assertTrue(resource.isDisposed)
    }

    @Test
    fun resource_is_not_disposed_WHEN_eager_is_false_and_upstream_disposed() {
        var isResourceDisposedBeforeInnerSingle by AtomicBoolean()

        val observer =
            singleUsing(eager = false) { resource ->
                singleUnsafe<Nothing> { observer ->
                    observer.onSubscribe(
                        Disposable {
                            isResourceDisposedBeforeInnerSingle = resource.isDisposed
                        }
                    )
                }
            }.test()

        observer.dispose()

        assertFalse(isResourceDisposedBeforeInnerSingle)
    }

    @Test
    fun resource_is_not_disposed_WHEN_eager_is_false_and_downstream_received_onComplete() {
        val resource = Disposable()
        val upstream = TestSingle<Int>()
        var isResourceDisposedBeforeInnerSingle by AtomicBoolean()

        val downstream = singleUsing(resourceSupplier = { resource }, eager = false, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultSingleObserver<Int> {
                override fun onSuccess(value: Int) {
                    isResourceDisposedBeforeInnerSingle = resource.isDisposed
                }
            }
        )

        upstream.onSuccess(0)

        assertFalse(isResourceDisposedBeforeInnerSingle)
    }

    @Test
    fun resource_is_not_disposed_WHEN_eager_is_false_and_downstream_received_onError() {
        val resource = Disposable()
        val upstream = TestSingle<Int>()
        var isResourceDisposedBeforeInnerSingle by AtomicBoolean()

        val downstream = singleUsing(resourceSupplier = { resource }, eager = false, sourceSupplier = { upstream })

        downstream.subscribe(
            object : DefaultSingleObserver<Int> {
                override fun onError(error: Throwable) {
                    isResourceDisposedBeforeInnerSingle = resource.isDisposed
                }
            }
        )

        upstream.onError(Exception())

        assertFalse(isResourceDisposedBeforeInnerSingle)
    }

    private fun singleUsing(
        resourceSupplier: () -> Disposable = ::Disposable,
        eager: Boolean,
        sourceSupplier: (resource: Disposable) -> Single<Int> = { TestSingle() },
    ): Single<Int> =
        singleUsing(
            resourceSupplier = resourceSupplier,
            resourceCleanup = Disposable::dispose,
            eager = eager,
            sourceSupplier = sourceSupplier,
        )
}
