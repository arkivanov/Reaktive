package com.badoo.reaktive.flowable

import com.badoo.reaktive.utils.SimpleCondition

interface FlowableValue<out T> {

    val value: T

    fun onProcessed()

    companion object {
        operator fun <T> invoke(value: T, condition: SimpleCondition): FlowableValue<T> =
            object : FlowableValue<T> {
                override val value: T = value

                override fun onProcessed() {
                    condition.signal()
                }
            }
    }
}