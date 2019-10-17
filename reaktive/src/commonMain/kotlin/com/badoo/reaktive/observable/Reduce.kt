package com.badoo.reaktive.observable

import com.badoo.reaktive.disposable.DisposableWrapper
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.maybeUnsafe
import com.badoo.reaktive.utils.ObjectReference

fun <T> Observable<T>.reduce(reducer: (a: T, b: T) -> T): Maybe<T> =
    maybeUnsafe { observer ->
        val disposableWrapper = DisposableWrapper()
        val prevValueHolder: ObjectReference<ObjectReference<T>?> = ObjectReference(null)

        subscribeSafe(
            downstreamObserver = observer,
            disposableContainer = disposableWrapper,
            onNext = { value ->
                val prevValueReference: ObjectReference<T>? = prevValueHolder.value
                if (prevValueReference == null) {
                    prevValueHolder.value = ObjectReference(value)
                } else {
                    try {
                        prevValueReference.value = reducer(prevValueReference.value, value)
                    } catch (e: Throwable) {
                        observer.onError(e)
                        disposableWrapper.dispose()
                    }
                }
            },
            onComplete = {
                val prevValue = prevValueHolder.value
                if (prevValue == null) {
                    observer.onComplete()
                } else {
                    observer.onSuccess(prevValue.value)
                }
            }
        )
    }
