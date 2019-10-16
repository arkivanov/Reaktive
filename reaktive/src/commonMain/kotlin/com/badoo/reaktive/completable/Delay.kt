package com.badoo.reaktive.completable

import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.scheduler.Scheduler

fun Completable.delay(delayMillis: Long, scheduler: Scheduler, delayError: Boolean = false): Completable =
    completableSafe(::CompositeDisposable) { callbacks, disposables ->
        val executor = scheduler.newExecutor()
        disposables += executor

        subscribeSafe(
            object : CompletableObserver {
                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
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
