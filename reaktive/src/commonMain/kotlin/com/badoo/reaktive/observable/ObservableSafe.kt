package com.badoo.reaktive.observable

import com.badoo.reaktive.disposable.Disposable

internal inline fun <T, D : Disposable> observableSafe(
    crossinline disposableFactory: () -> D,
    crossinline onSubscribe: (callbacks: ObservableCallbacks<T>, disposable: D) -> Unit
): Observable<T> =
    observableUnsafe { observer ->
        val disposable = disposableFactory()
        observer.onSubscribe(disposable)

        val safeCallbacks = SafeObservableCallbacks(observer, disposable)

        try {
            onSubscribe(safeCallbacks, disposable)
        } catch (e: Throwable) {
            safeCallbacks.onError(e)
        }
    }

internal open class SafeObservableCallbacks<T>(
    private val delegate: ObservableCallbacks<T>,
    private val disposable: Disposable
) : ObservableCallbacks<T> {
    override fun onNext(value: T) {
        if (!disposable.isDisposed) {
            delegate.onNext(value)
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
