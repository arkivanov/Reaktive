package com.badoo.reaktive.maybe

import com.badoo.reaktive.disposable.DisposableWrapper
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableUnsafe

fun <T> Maybe<T>.asObservable(): Observable<T> =
    observableUnsafe { observer ->
        val disposableWrapper = DisposableWrapper()

        subscribeSafe(
            downstreamObserver = observer,
            onSuccess = {
                observer.onNext(it)
                if (!disposableWrapper.isDisposed) {
                    observer.onComplete()
                }
            },
            onComplete = observer::onComplete
        )
    }