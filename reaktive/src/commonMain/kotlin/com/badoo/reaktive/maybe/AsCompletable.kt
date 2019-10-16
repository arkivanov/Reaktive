package com.badoo.reaktive.maybe

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableUnsafe

fun Maybe<*>.asCompletable(): Completable =
    completableUnsafe { observer ->
        subscribeSafe(
            downstreamObserver = observer,
            onSuccess = { observer.onComplete() },
            onComplete = observer::onComplete
        )
    }