package com.badoo.reaktive.completable

import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.utils.freeze

fun Completable.observeOn(scheduler: Scheduler): Completable =
    completableSafe(::CompositeDisposable) { callbacks, disposables ->
        val executor = scheduler.newExecutor()
        disposables += executor

        subscribeSafe(
            object : CompletableObserver {
                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
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