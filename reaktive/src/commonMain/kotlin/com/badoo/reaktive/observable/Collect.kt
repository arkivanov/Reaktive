package com.badoo.reaktive.observable

import com.badoo.reaktive.disposable.DisposableWrapper
import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.singleUnsafe
import com.badoo.reaktive.utils.ObjectReference

fun <T, C> Observable<T>.collect(initialCollection: C, accumulator: (C, T) -> C): Single<C> =
    singleUnsafe { observer ->
        val collection = ObjectReference(initialCollection)
        val disposableWrapper = DisposableWrapper()

        subscribeSafe(
            downstreamObserver = observer,
            disposableContainer = disposableWrapper,
            onNext = { value ->
                try {
                    collection.value = accumulator(collection.value, value)
                } catch (e: Throwable) {
                    observer.onError(e)
                    disposableWrapper.dispose()
                }
            },
            onComplete = { observer.onSuccess(collection.value) }
        )
    }
