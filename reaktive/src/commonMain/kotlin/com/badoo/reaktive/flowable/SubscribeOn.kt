package com.badoo.reaktive.flowable

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.scheduler.Scheduler

fun <T> Flowable<T>.subscribeOn(scheduler: Scheduler): Flowable<T> =
    flowableUnsafe { observer ->
        val disposables = CompositeDisposable()
        observer.onSubscribe(disposables)
        val executor = scheduler.newExecutor()
        disposables += executor

        executor.submit {
            subscribeSafe(
                object : FlowableObserver<T> by observer {
                    override fun onSubscribe(disposable: Disposable) {
                        disposables += disposable
                    }
                }
            )
        }
    }