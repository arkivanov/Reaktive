package com.badoo.reaktive.observable

import com.badoo.reaktive.base.exceptions.CompositeException
import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.utils.ThreadLocalStorage
import com.badoo.reaktive.utils.handleSourceError

fun <T> Observable<T>.threadLocal(): Observable<T> =
    observableSafe(::CompositeDisposable) { callbacks, disposables ->
        val callbacksStorage = ThreadLocalStorage(callbacks)
        disposables += callbacksStorage

        subscribeSafe(
            object : ObservableObserver<T> {
                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onNext(value: T) {
                    getCallbacks()?.onNext(value)
                }

                override fun onComplete() {
                    getCallbacks()?.onComplete()
                }

                override fun onError(error: Throwable) {
                    getCallbacks(error)?.onError(error)
                }

                private fun getCallbacks(existingError: Throwable? = null): ObservableCallbacks<T>? =
                    try {
                        requireNotNull(callbacksStorage.get())
                    } catch (e: Throwable) {
                        handleSourceError(if (existingError == null) e else CompositeException(existingError, e))
                        null
                    }
            }
        )
    }