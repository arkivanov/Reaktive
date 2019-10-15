package com.badoo.reaktive.observable

import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.DisposableWrapper
import com.badoo.reaktive.disposable.doIfNotDisposed
import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.singleUnsafe
import com.badoo.reaktive.utils.ObjectReference

fun <T, C> Observable<T>.collect(initialCollection: C, accumulator: (C, T) -> C): Single<C> =
    singleUnsafe(::DisposableWrapper) { observer, disposableWrapper ->
        subscribeSafe(
            object : ObservableObserver<T> {
                private val collection = ObjectReference(initialCollection)

                override fun onSubscribe(disposable: Disposable) {
                    disposableWrapper.set(disposable)
                }

                override fun onNext(value: T) {
                    collection.value =
                        try {
                            accumulator(collection.value, value)
                        } catch (e: Throwable) {
                            onError(e)
                            return
                        }
                }

                override fun onComplete() {
                    disposableWrapper.doIfNotDisposed(dispose = true) {
                        observer.onSuccess(collection.value)
                    }
                }

                override fun onError(error: Throwable) {
                    disposableWrapper.doIfNotDisposed(dispose = true) {
                        observer.onError(error)
                    }
                }
            }
        )
    }
