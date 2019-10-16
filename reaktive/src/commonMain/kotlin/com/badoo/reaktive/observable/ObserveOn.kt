package com.badoo.reaktive.observable

import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.scheduler.BufferedExecutor
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.utils.freeze

fun <T> Observable<T>.observeOn(scheduler: Scheduler): Observable<T> =
    observableSafe(::CompositeDisposable) { callbacks, disposables ->
        val executor = scheduler.newExecutor()
        disposables += executor

        subscribeSafe(
            object : ObservableObserver<T> {
                private val bufferedExecutor = BufferedExecutor(executor, callbacks::onNext)

                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onNext(value: T) {
                    bufferedExecutor.submit(value)
                }

                override fun onComplete() {
                    executor.submit {
                        callbacks.onComplete()
                    }
                }

                override fun onError(error: Throwable) {
                    error.freeze()

                    executor.submit {
                        callbacks.onError(error)
                    }
                }
            }
        )
    }