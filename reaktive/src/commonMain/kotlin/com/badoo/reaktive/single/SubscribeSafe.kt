package com.badoo.reaktive.single

import com.badoo.reaktive.base.ErrorCallback
import com.badoo.reaktive.base.Observer
import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.DisposableContainer
import com.badoo.reaktive.disposable.DisposableWrapper

internal inline fun <T, O> Single<T>.subscribeSafe(
    downstreamObserver: O,
    disposableContainer: DisposableContainer = DisposableWrapper(),
    crossinline onSuccess: (T) -> Unit = {},
    crossinline onError: (Throwable) -> Unit = downstreamObserver::onError
) where O : Observer, O : ErrorCallback {
    downstreamObserver.onSubscribe(disposableContainer)

    subscribeSafe(
        object : SingleObserver<T> {
            override fun onSubscribe(disposable: Disposable) {
                disposableContainer.accept(disposable)
            }

            override fun onSuccess(value: T) {
                if (!disposableContainer.isDisposed) {
                    onSuccess.invoke(value)
                    disposableContainer.dispose()
                }
            }

            override fun onError(error: Throwable) {
                if (!disposableContainer.isDisposed) {
                    onError.invoke(error)
                    disposableContainer.dispose()
                }
            }
        }
    )
}
