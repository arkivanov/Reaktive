package com.badoo.reaktive.completable

import com.badoo.reaktive.base.ErrorCallback
import com.badoo.reaktive.base.Observer
import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.DisposableContainer
import com.badoo.reaktive.disposable.DisposableWrapper

internal inline fun <O> Completable.subscribeSafe(
    downstreamObserver: O,
    disposableContainer: DisposableContainer = DisposableWrapper(),
    crossinline onComplete: () -> Unit = {},
    crossinline onError: (Throwable) -> Unit = downstreamObserver::onError
) where O : Observer, O : ErrorCallback {
    downstreamObserver.onSubscribe(disposableContainer)

    subscribeSafe(
        object : CompletableObserver {
            override fun onSubscribe(disposable: Disposable) {
                disposableContainer.accept(disposable)
            }

            override fun onComplete() {
                if (!disposableContainer.isDisposed) {
                    onComplete()
                    disposableContainer.dispose()
                }
            }

            override fun onError(error: Throwable) {
                if (!disposableContainer.isDisposed) {
                    onError(error)
                    disposableContainer.dispose()
                }
            }
        }
    )
}
