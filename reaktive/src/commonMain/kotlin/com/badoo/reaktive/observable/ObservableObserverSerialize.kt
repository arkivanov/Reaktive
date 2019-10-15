package com.badoo.reaktive.observable

import com.badoo.reaktive.base.Observer
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.utils.serializer.serializer

internal fun <T> ObservableObserver<T>.serialize(disposable: Disposable): ObservableObserver<T> =
    SerializedObservableObserver(this, disposable)

private class SerializedObservableObserver<T>(
    private val delegate: ObservableObserver<T>,
    private val disposable: Disposable
) : ObservableObserver<T>, Observer by delegate {

    private val serializer =
        serializer<Any?> { event ->
            if (disposable.isDisposed) {
                return@serializer false
            }

            if (event is Event) {
                when (event) {
                    is Event.OnComplete -> delegate.onComplete()
                    is Event.OnError -> delegate.onError(event.error)
                }
                disposable.dispose()

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
