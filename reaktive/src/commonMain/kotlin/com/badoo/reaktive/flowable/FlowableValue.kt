package com.badoo.reaktive.flowable

interface FlowableValue<out T> {

    val value: T

    fun onProcessed()

    companion object {
        inline operator fun <T> invoke(value: T, crossinline onProcessed: () -> Unit): FlowableValue<T> =
            object : FlowableValue<T> {
                override val value: T = value

                override fun onProcessed() {
                    onProcessed.invoke()
                }
            }
    }
}