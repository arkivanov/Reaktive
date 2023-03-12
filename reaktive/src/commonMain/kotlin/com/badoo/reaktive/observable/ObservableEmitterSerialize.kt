package com.badoo.reaktive.observable

import com.badoo.reaktive.base.Emitter
import com.badoo.reaktive.utils.serializer.DefaultSerializer

fun <T> ObservableEmitter<T>.serialize(): ObservableEmitter<T> =
    SerializedObservableEmitter(this)

private open class SerializedObservableEmitter<in T>(
    private val delegate: ObservableEmitter<T>
) : DefaultSerializer<Any?>(), ObservableEmitter<T>, Emitter by delegate {

    override fun onComplete() {
        accept(Event.Complete)
    }

    override fun onError(error: Throwable) {
        accept(Event.Error(error))
    }

    override fun onNext(value: T) {
        accept(value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onValue(value: Any?): Boolean =
        when {
            value == null -> delegate.onNext(null as T).let { true }
            value::class == Event.Complete::class -> delegate.onComplete().let { false }
            value::class == Event.Error::class -> delegate.onError((value as Event.Error).error).let { false }
            else -> delegate.onNext(value as T).let { true }
        }

    private sealed interface Event {
        object Complete : Event
        class Error(val error: Throwable) : Event
    }
}
