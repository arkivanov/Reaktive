package com.badoo.reaktive.maybe

import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.scheduler.Scheduler

fun <T> Maybe<T>.delay(delayMillis: Long, scheduler: Scheduler, delayError: Boolean = false): Maybe<T> =
    maybeSafe(::CompositeDisposable) { callbacks, disposables ->
        val executor = scheduler.newExecutor()
        disposables += executor

        subscribeSafe(
            object : MaybeObserver<T> {
                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onSuccess(value: T) {
                    executor.submit(delayMillis) {
                        callbacks.onSuccess(value)
                    }
                }

                override fun onComplete() {
                    executor.submit(delayMillis, callbacks::onComplete)
                }

                override fun onError(error: Throwable) {
                    if (delayError) {
                        executor.submit(delayMillis) {
                            callbacks.onError(error)
                        }
                    } else {
                        executor.cancel()
                        executor.submit {
                            callbacks.onError(error)
                        }
                    }
                }
            }
        )
    }
