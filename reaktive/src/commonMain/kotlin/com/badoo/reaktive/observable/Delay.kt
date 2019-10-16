package com.badoo.reaktive.observable

import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.scheduler.Scheduler

fun <T> Observable<T>.delay(delayMillis: Long, scheduler: Scheduler, delayError: Boolean = false): Observable<T> =
    observableSafe(::CompositeDisposable) { callbacks, disposables ->
        val executor = scheduler.newExecutor()
        disposables += executor

        subscribeSafe(
            object : ObservableObserver<T> {
                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onNext(value: T) {
                    executor.submit(delayMillis) {
                        callbacks.onNext(value)
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
