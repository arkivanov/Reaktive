package com.badoo.reaktive.completable

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableUnsafe

fun <T> Completable.asObservable(): Observable<T> =
    observableUnsafe { observer ->
        subscribeSafe(
            downstreamObserver = observer,
            onComplete = observer::onComplete
        )
    }