package com.badoo.reaktive.single

import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.scheduler.Scheduler

fun <T> Single<T>.subscribeOn(scheduler: Scheduler): Single<T> =
    singleSafe(::CompositeDisposable) { callbacks, disposables ->
        val executor = scheduler.newExecutor()
        disposables += executor

        executor.submit {
            subscribeSafe(
                object : SingleObserver<T>, SingleCallbacks<T> by callbacks {
                    override fun onSubscribe(disposable: Disposable) {
                        disposables += disposable
                    }
                }
            )
        }
    }