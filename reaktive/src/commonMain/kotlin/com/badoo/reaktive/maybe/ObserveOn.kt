package com.badoo.reaktive.maybe

import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.utils.freeze

fun <T> Maybe<T>.observeOn(scheduler: Scheduler): Maybe<T> =
    maybeSafe(::CompositeDisposable) { callbacks, disposables ->
        val executor = scheduler.newExecutor()
        disposables += executor

        subscribeSafe(
            object : MaybeObserver<T> {
                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onSuccess(value: T) {
                    executor.submit {
                        callbacks.onSuccess(value)
                    }
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