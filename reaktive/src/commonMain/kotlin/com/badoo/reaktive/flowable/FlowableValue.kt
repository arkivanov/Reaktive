package com.badoo.reaktive.flowable

import com.badoo.reaktive.utils.SimpleCondition

interface FlowableValue<out T> {

    val value: T

    fun onProcessed()

    fun await()

    companion object {
        internal operator fun <T> invoke(value: T): FlowableValue<T> =
            object : FlowableValue<T> {
                private val condition = SimpleCondition()
                override val value: T = value

                override fun onProcessed() {
                    condition.signal()
                }

                override fun await() {
                    condition.await()
                }
            }
    }
}