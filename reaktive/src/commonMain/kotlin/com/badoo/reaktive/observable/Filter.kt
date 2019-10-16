package com.badoo.reaktive.observable

import com.badoo.reaktive.disposable.DisposableWrapper

fun <T> Observable<T>.filter(predicate: (T) -> Boolean): Observable<T> =
    observableUnsafe { observer ->
        val disposableWrapper = DisposableWrapper()

        subscribeSafe(
            downstreamObserver = observer,
            disposableContainer = disposableWrapper,
            onNext = {
                try {
                    if (predicate(it) && !disposableWrapper.isDisposed) {
                        observer.onNext(it)
                    }
                } catch (e: Throwable) {
                    if (!disposableWrapper.isDisposed) {
                        observer.onError(e)
                    }
                }
            },
            onComplete = observer::onComplete
        )
    }