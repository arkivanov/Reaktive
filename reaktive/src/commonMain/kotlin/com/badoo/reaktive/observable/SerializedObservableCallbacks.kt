package com.badoo.reaktive.observable

import com.badoo.reaktive.utils.serializer.DefaultSerializer

internal open class SerializedObservableCallbacks<in T>(
    private val delegate: ObservableCallbacks<T>,
) : ObservableCallbacks<T>, DefaultSerializer<Any?, SerializedObservableCallbacks.Token>() {

    override fun onComplete() {
        accept(value = null, token = Token.COMPLETE)
    }

    override fun onError(error: Throwable) {
        accept(value = error, token = Token.COMPLETE)
    }

    override fun onNext(value: T) {
        accept(value = value, token = Token.VALUE)
    }

    override fun onValue(value: Any?, token: Token): Boolean =
        when (token) {
            Token.VALUE -> delegate.onNext(value as T).let { true }
            Token.COMPLETE -> delegate.onComplete().let { false }
            Token.ERROR -> delegate.onError(value as Throwable).let { false }
        }

    enum class Token {
        VALUE, COMPLETE, ERROR
    }
}
