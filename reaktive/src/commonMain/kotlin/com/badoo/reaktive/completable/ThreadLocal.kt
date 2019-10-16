package com.badoo.reaktive.completable

import com.badoo.reaktive.base.exceptions.CompositeException
import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.utils.ThreadLocalStorage
import com.badoo.reaktive.utils.handleSourceError

fun Completable.threadLocal(): Completable =
    completableSafe(::CompositeDisposable) { callbacks, disposables ->
        val callbacksStorage = ThreadLocalStorage(callbacks)
        disposables += callbacksStorage

        subscribeSafe(
            object : CompletableObserver {
                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onComplete() {
                    getCallbacks()?.onComplete()
                }

                override fun onError(error: Throwable) {
                    getCallbacks(error)?.onError(error)
                }

                private fun getCallbacks(existingError: Throwable? = null): CompletableCallbacks? =
                    try {
                        requireNotNull(callbacksStorage.get())
                    } catch (e: Throwable) {
                        handleSourceError(if (existingError == null) e else CompositeException(existingError, e))
                        null
                    }
            }
        )
    }