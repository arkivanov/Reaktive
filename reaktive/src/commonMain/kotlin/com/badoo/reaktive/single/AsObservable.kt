package com.badoo.reaktive.single

import com.badoo.reaktive.disposable.DisposableWrapper
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableUnsafe

fun <T> Single<T>.asObservable(): Observable<T> =
    observableUnsafe { observer ->
        val disposableWrapper = DisposableWrapper()

        subscribeSafe(
            downstreamObserver = observer,
            disposableContainer = disposableWrapper,
            onSuccess = { value ->
                observer.onNext(value)
                if (!disposableWrapper.isDisposed) {
                    observer.onComplete()
                }
            }
        )
    }