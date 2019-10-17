package com.badoo.reaktive.maybe

import com.badoo.reaktive.base.ErrorCallback
import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableSafe
import com.badoo.reaktive.observable.serialize
import com.badoo.reaktive.utils.atomic.AtomicInt

fun <T> Iterable<Maybe<T>>.merge(): Observable<T> =
    observableSafe(::CompositeDisposable) { callbacks, disposables ->
        val serializedCallbacks = callbacks.serialize()
        val activeUpstreamCount = AtomicInt(1)

        val upstreamObserver: MaybeObserver<T> =
            object : MaybeObserver<T>, ErrorCallback by serializedCallbacks {
                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onSuccess(value: T) {
                    serializedCallbacks.onNext(value)
                    onComplete()
                }

                override fun onComplete() {
                    if (activeUpstreamCount.addAndGet(-1) == 0) {
                        serializedCallbacks.onComplete()
                    }
                }
            }

        forEach { upstream ->
            activeUpstreamCount.addAndGet(1)
            upstream.subscribeSafe(upstreamObserver)
        }

        upstreamObserver.onComplete()
    }

fun <T> merge(vararg sources: Maybe<T>): Observable<T> =
    sources
        .asIterable()
        .merge()