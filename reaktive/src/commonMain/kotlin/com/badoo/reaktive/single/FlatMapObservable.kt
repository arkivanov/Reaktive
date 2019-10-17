package com.badoo.reaktive.single

import com.badoo.reaktive.base.ErrorCallback
import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.DisposableWrapper
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableCallbacks
import com.badoo.reaktive.observable.ObservableObserver
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.observableSafe

fun <T, R> Single<T>.flatMapObservable(mapper: (T) -> Observable<R>): Observable<R> =
    observableSafe(::DisposableWrapper) { callbacks, disposableWrapper ->
        subscribeSafe(
            object : SingleObserver<T>, ErrorCallback by callbacks {
                override fun onSubscribe(disposable: Disposable) {
                    disposableWrapper.set(disposable)
                }

                override fun onSuccess(value: T) {
                    val inner =
                        try {
                            mapper(value)
                        } catch (e: Throwable) {
                            callbacks.onError(e)
                            return
                        }

                    inner.subscribeSafe(
                        object : ObservableObserver<R>, ObservableCallbacks<R> by callbacks {
                            override fun onSubscribe(disposable: Disposable) {
                                disposableWrapper.set(disposable)
                            }
                        }
                    )
                }
            }
        )
    }

fun <T, U, R> Single<T>.flatMapObservable(mapper: (T) -> Observable<U>, resultSelector: (T, U) -> R): Observable<R> =
    flatMapObservable { t ->
        mapper(t).map { u -> resultSelector(t, u) }
    }