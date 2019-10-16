package com.badoo.reaktive.single

import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.maybeUnsafe

fun <T> Single<T>.asMaybe(): Maybe<T> =
    maybeUnsafe { observer ->
        subscribeSafe(downstreamObserver = observer, onSuccess = observer::onSuccess)
    }