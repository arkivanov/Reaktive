package com.badoo.reaktive.flowable

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.scheduler.Scheduler

fun <T> Flowable<T>.observeOn(
    scheduler: Scheduler,
    backPressureStrategy: BackPressureStrategy = BackPressureStrategy()
): Flowable<T> =
    flowableSafe { observer ->
        val disposables = CompositeDisposable()
        observer.onSubscribe(disposables)
        val executor = scheduler.newExecutor()
        disposables += executor

        subscribeSafe(
            object : FlowableObserver<T> {
                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onNext(value: FlowableValue<T>) {
                    executor.submit {
                        observer.onNext(value)
                    }
                }

                override fun onComplete() {
                    executor.submit {
                        observer.onComplete()
                    }
                }

                override fun onError(error: Throwable) {
                    executor.submit {
                        observer.onError(error)
                    }
                }
            }
        )
    }

private data class State<out T>(
    val buffer: List<T> = emptyList(),
    val isDraining: Boolean = false
)