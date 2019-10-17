package com.badoo.reaktive.maybe

import com.badoo.reaktive.test.observable.assertValues
import com.badoo.reaktive.test.observable.test
import kotlin.test.Test

class MergeTest {

    @Test
    fun foo() {
        val list = listOf(maybeOf(1), maybeOf(2))

        val observer = list.merge().test()

        observer.assertValues(1, 2)
    }
}