package com.badoo.reaktive.flowable

import com.badoo.reaktive.completable.CompletableCallbacks
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.utils.atomicreference.AtomicReference
import com.badoo.reaktive.utils.serializer.Serializer
import com.badoo.reaktive.utils.serializer.serializer

fun <T> FlowableObserver<T>.safe(): FlowableObserver<T> =
    object : FlowableObserver<T> {
        private lateinit var disposable: Disposable

        override fun onSubscribe(disposable: Disposable) {
            this.disposable = disposable
            this@safe.onSubscribe(disposable)
        }

        override fun onNext(value: FlowableValue<T>) {
            if (!disposable.isDisposed) {
                this@safe.onNext(value)
            } else {
                value.onProcessed()
            }
        }

        override fun onComplete() {
            if (!disposable.isDisposed) {
                try {
                    this@safe.onComplete()
                } finally {
                    disposable.dispose()
                }
            }
        }

        override fun onError(error: Throwable) {
            if (!disposable.isDisposed) {
                try {
                    this@safe.onError(error)
                } finally {
                    disposable.dispose()
                }
            }
        }
    }

fun <T> FlowableCallbacks<FlowableValue<T>>.blocking(): FlowableCallbacks<T> =
    object : FlowableCallbacks<T>, CompletableCallbacks by this {
        override fun onNext(value: T) {
            val flowableValue = FlowableValue(value)
            this@blocking.onNext(flowableValue)
            flowableValue.await()
        }
    }

fun <T> FlowableCallbacks<FlowableValue<T>>.serialize(): FlowableCallbacks<FlowableValue<T>> =
    object : FlowableCallbacks<FlowableValue<T>> {
        private val serializer: Serializer<Any?> =
            serializer { event ->
                if (event is FlowableObserverSerializeEvent) {
                    when (event) {
                        is FlowableObserverSerializeEvent.OnComplete -> this@serialize.onComplete()
                        is FlowableObserverSerializeEvent.OnError -> this@serialize.onError(event.error)
                    }

                    false
                } else {
                    val value = event as FlowableValue<T>
                    this@serialize.onNext(value)
                    value.await()

                    true
                }
            }

        override fun onNext(value: FlowableValue<T>) {
            serializer.accept(value)
        }

        override fun onComplete() {
            serializer.accept(FlowableObserverSerializeEvent.OnComplete)
        }

        override fun onError(error: Throwable) {
            serializer.accept(FlowableObserverSerializeEvent.OnError(error))
        }
    }

private sealed class FlowableObserverSerializeEvent {
    object OnComplete : FlowableObserverSerializeEvent()
    class OnError(val error: Throwable) : FlowableObserverSerializeEvent()
}