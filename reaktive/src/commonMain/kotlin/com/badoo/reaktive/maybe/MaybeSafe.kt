package com.badoo.reaktive.maybe

import com.badoo.reaktive.disposable.Disposable

internal inline fun <T, D : Disposable> maybeSafe(
    crossinline disposableFactory: () -> D,
    crossinline onSubscribe: (callbacks: MaybeCallbacks<T>, disposable: D) -> Unit
): Maybe<T> =
    maybeUnsafe { observer ->
        val disposable = disposableFactory()
        observer.onSubscribe(disposable)

        val safeCallbacks = SafeMaybeCallbacks(observer, disposable)

        try {
            onSubscribe(safeCallbacks, disposable)
        } catch (e: Throwable) {
            safeCallbacks.onError(e)
        }
    }

internal open class SafeMaybeCallbacks<T>(
    private val delegate: MaybeCallbacks<T>,
    private val disposable: Disposable
) : MaybeCallbacks<T> {
    override fun onSuccess(value: T) {
        if (!disposable.isDisposed) {
            delegate.onSuccess(value)
        }
    }

    override fun onComplete() {
        if (!disposable.isDisposed) {
            delegate.onComplete()
            disposable.dispose()
        }
    }

    override fun onError(error: Throwable) {
        if (!disposable.isDisposed) {
            delegate.onError(error)
            disposable.dispose()
        }
    }
}
