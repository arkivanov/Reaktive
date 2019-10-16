package com.badoo.reaktive.observable

import com.badoo.reaktive.utils.serializer.serializer

internal fun <T> ObservableCallbacks<T>.serialize(): ObservableCallbacks<T> = SerializedObservableCallbacks(this)

private class SerializedObservableCallbacks<T>(
    private val delegate: ObservableCallbacks<T>
) : ObservableCallbacks<T> {

    private val serializer =
        serializer<Any?> { event ->
            if (event is Event) {
                when (event) {
                    is Event.OnComplete -> delegate.onComplete()
                    is Event.OnError -> delegate.onError(event.error)
                }

                false
            } else {
                @Suppress("UNCHECKED_CAST") // Either Event or T to avoid unnecessary allocations
                delegate.onNext(event as T)

                true
            }
        }

    override fun onNext(value: T) {
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