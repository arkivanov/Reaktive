package com.badoo.reaktive.observable

import com.badoo.reaktive.base.Observer
import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.completable.CompletableCallbacks
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable

fun <T> Observable<T>.takeUntil(other: Observable<*>): Observable<T> =
    observableSafe(::CompositeDisposable) { callbacks, disposables ->
        val serializedCallbacks = callbacks.serialize()

        val upstreamObserver =
            object : ObservableObserver<T>, ObservableCallbacks<T> by serializedCallbacks {
                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }
            }

        other.subscribeSafe(
            object : ObservableObserver<Any?>, Observer by upstreamObserver, CompletableCallbacks by upstreamObserver {
                override fun onNext(value: Any?) {
                    upstreamObserver.onComplete()
                }
            }
        )

        subscribeSafe(upstreamObserver)
    }
