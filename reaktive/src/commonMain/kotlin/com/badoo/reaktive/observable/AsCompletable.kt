package com.badoo.reaktive.observable

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableUnsafe

fun Observable<*>.asCompletable(): Completable =
    completableUnsafe { observer ->
        subscribeSafe(downstreamObserver = observer, onComplete = observer::onComplete)
    }