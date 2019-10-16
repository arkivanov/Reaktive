package com.badoo.reaktive.completable

import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.scheduler.Scheduler

fun Completable.subscribeOn(scheduler: Scheduler): Completable =
    completableSafe(::CompositeDisposable) { callbacks, disposables ->
        val executor = scheduler.newExecutor()
        disposables += executor

        executor.submit {
            subscribeSafe(
                object : CompletableObserver, CompletableCallbacks by callbacks {
                    override fun onSubscribe(disposable: Disposable) {
                        disposables += disposable
                    }
                }
            )
        }
    }