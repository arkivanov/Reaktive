package com.badoo.reaktive.flowable

import com.badoo.reaktive.base.Observer
import com.badoo.reaktive.utils.serializer.Serializer
import com.badoo.reaktive.utils.serializer.serializer

fun <T> FlowableObserver<T>.serialize(): FlowableObserver<T> = SerializedFlowableObserver(this)

private class SerializedFlowableObserver<in T>(
    private val delegate: FlowableObserver<T>
) : FlowableObserver<T>, Observer by delegate {

    private val serializer: Serializer<Any?> =
        serializer { event ->
            if (event is Event) {
                when (event) {
                    is Event.OnComplete -> delegate.onComplete()
                    is Event.OnError -> delegate.onError(event.error)
                }

                false
            } else {
                @Suppress("UNCHECKED_CAST") // Either Event or T to avoid unnecessary allocations
                val value = event as FlowableValue<T>
                delegate.onNextBlocking(value.value)
                value.onProcessed()

                true
            }
        }

    override fun onNext(value: FlowableValue<T>) {
        serializer.accept(value)
    }

    override fun onComplete() {
        serializer.accept(Event.OnComplete)
    }

    override fun onError(error: Throwable) {
        serializer.accept(Event.OnError(error))
    }

    private sealed class Event {
        object OnComplete : Event()
        class OnError(val error: Throwable) : Event()
    }
}