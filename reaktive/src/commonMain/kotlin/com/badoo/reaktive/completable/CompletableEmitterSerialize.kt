package com.badoo.reaktive.completable

import com.badoo.reaktive.base.Emitter
import com.badoo.reaktive.utils.serializer.DefaultSerializer
import com.badoo.reaktive.utils.serializer.accept

fun CompletableEmitter.serialize(): CompletableEmitter = SerializedCompletableEmitter(this)

private class SerializedCompletableEmitter(
    private val delegate: CompletableEmitter
) : CompletableEmitter, DefaultSerializer<SerializedCompletableEmitter.Event, Nothing?>(), Emitter by delegate {

    override fun onComplete() {
        accept(Event.OnComplete)
    }

    override fun onError(error: Throwable) {
        accept(Event.OnError(error))
    }

    override fun onValue(value: Event, token: Nothing?): Boolean {
        when (value) {
            Event.OnComplete -> delegate.onComplete()
            is Event.OnError -> delegate.onError(value.error)
        }

        return false
    }

    sealed class Event {
        object OnComplete : Event()
        class OnError(val error: Throwable) : Event()
    }
}
