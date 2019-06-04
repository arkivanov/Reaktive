package com.badoo.reaktive.single

import com.badoo.reaktive.test.observable.isCompleted
import com.badoo.reaktive.test.observable.test
import com.badoo.reaktive.test.observable.values
import com.badoo.reaktive.test.single.TestSingle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AsObservableTest {

    private val upstream = TestSingle<Int?>()
    private val observer = upstream.asObservable().test()

    @Test
    fun emits_same_value_and_completes_WHEN_upstream_succeeded_with_non_null_value() {
        upstream.onSuccess(0)

        assertEquals(listOf(0), observer.values)
        assertTrue(observer.isCompleted)
    }

    @Test
    fun emits_null_and_completes_WHEN_upstream_succeeded_with_null_value() {
        upstream.onSuccess(null)

        assertEquals(listOf(null), observer.values)
        assertTrue(observer.isCompleted)
    }
}