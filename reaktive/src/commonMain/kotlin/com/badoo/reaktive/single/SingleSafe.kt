package com.badoo.reaktive.single

import com.badoo.reaktive.disposable.Disposable

internal inline fun <T, D : Disposable> singleSafe(
    crossinline disposableFactory: () -> D,
    crossinline onSubscribe: (callbacks: SingleCallbacks<T>, disposable: D) -> Unit
): Single<T> =
    singleUnsafe { observer ->
        val disposable = disposableFactory()
        observer.onSubscribe(disposable)

        val safeCallbacks = SafeSingleCallbacks(observer, disposable)

        try {
            onSubscribe(safeCallbacks, disposable)
        } catch (e: Throwable) {
            safeCallbacks.onError(e)
        }
    }

internal open class SafeSingleCallbacks<T>(
    private val delegate: SingleCallbacks<T>,
    private val disposable: Disposable
) : SingleCallbacks<T> {
    override fun onSuccess(value: T) {
        if (!disposable.isDisposed) {
            delegate.onSuccess(value)
        }
    }

    override fun onError(error: Throwable) {
        if (!disposable.isDisposed) {
            delegate.onError(error)
            disposable.dispose()
        }
    }
}
