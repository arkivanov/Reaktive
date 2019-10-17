package com.badoo.reaktive.maybe

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.DisposableWrapper

fun <T, R> Maybe<T>.flatMap(mapper: (T) -> Maybe<R>): Maybe<R> =
    maybeUnsafe { observer ->
        val disposableWrapper = DisposableWrapper()

        val innerObserver =
            object : SafeMaybeCallbacks<R>(observer, disposableWrapper), MaybeObserver<R> {
                override fun onSubscribe(disposable: Disposable) {
                    disposableWrapper.set(disposable)
                }
            }

        subscribeSafe(
            downstreamObserver = observer,
            disposableContainer = disposableWrapper,
            onSuccess = { value ->
                try {
                    mapper(value).subscribe(innerObserver)
                } catch (e: Throwable) {
                    observer.onError(e)
                    disposableWrapper.dispose()
                }
            }
        )
    }

fun <T, U, R> Maybe<T>.flatMap(mapper: (T) -> Maybe<U>, resultSelector: (T, U) -> R): Maybe<R> =
    flatMap { t ->
        mapper(t).map { u -> resultSelector(t, u) }
    }