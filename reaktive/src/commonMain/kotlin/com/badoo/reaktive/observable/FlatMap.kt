package com.badoo.reaktive.observable

import com.badoo.reaktive.base.ErrorCallback
import com.badoo.reaktive.base.Observer
import com.badoo.reaktive.base.ValueCallback
import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.completable.CompletableCallbacks
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.utils.atomic.AtomicInt

fun <T, R> Observable<T>.flatMap(mapper: (T) -> Observable<R>): Observable<R> =
    observableUnsafe(::CompositeDisposable) { observer, disposables ->
        val serializedObserver = observer.serialize(disposables)

        subscribeSafe(
            object : ObservableObserver<T>, ErrorCallback by serializedObserver {
                private val activeSourceCount = AtomicInt(1)

                private val mappedObserver: ObservableObserver<R> =
                    object : ObservableObserver<R>, Observer by this, CompletableCallbacks by this,
                        ValueCallback<R> by serializedObserver {
                    }

                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onNext(value: T) {
                    activeSourceCount.addAndGet(1)

                    try {
                        mapper(value).subscribe(mappedObserver)
                    } catch (e: Throwable) {
                        serializedObserver.onError(e)
                    }
                }

                override fun onComplete() {
                    if (activeSourceCount.addAndGet(-1) <= 0) {
                        serializedObserver.onComplete()
                    }
                }
            }
        )
    }

fun <T, U, R> Observable<T>.flatMap(mapper: (T) -> Observable<U>, resultSelector: (T, U) -> R): Observable<R> =
    flatMap { t ->
        mapper(t).map { u -> resultSelector(t, u) }
    }
