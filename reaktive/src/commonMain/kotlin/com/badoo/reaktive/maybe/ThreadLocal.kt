package com.badoo.reaktive.maybe

import com.badoo.reaktive.base.exceptions.CompositeException
import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.utils.ThreadLocalStorage
import com.badoo.reaktive.utils.handleSourceError

fun <T> Maybe<T>.threadLocal(): Maybe<T> =
    maybeSafe(::CompositeDisposable) { callbacks, disposables ->
        val callbacksStorage = ThreadLocalStorage(callbacks)
        disposables += callbacksStorage

        subscribeSafe(
            object : MaybeObserver<T> {
                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onSuccess(value: T) {
                    getCallbacks()?.onSuccess(value)
                }

                override fun onComplete() {
                    getCallbacks()?.onComplete()
                }

                override fun onError(error: Throwable) {
                    getCallbacks(error)?.onError(error)
                }

                private fun getCallbacks(existingError: Throwable? = null): MaybeCallbacks<T>? =
                    try {
                        requireNotNull(callbacksStorage.get())
                    } catch (e: Throwable) {
                        handleSourceError(if (existingError == null) e else CompositeException(existingError, e))
                        null
                    }
            }
        )
    }