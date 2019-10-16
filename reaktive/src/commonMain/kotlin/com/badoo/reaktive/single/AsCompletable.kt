package com.badoo.reaktive.single

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableUnsafe

fun Single<*>.asCompletable(): Completable =
    completableUnsafe { observer ->
        subscribeSafe(
            downstreamObserver = observer,
            onSuccess = { observer.onComplete() }
        )
    }