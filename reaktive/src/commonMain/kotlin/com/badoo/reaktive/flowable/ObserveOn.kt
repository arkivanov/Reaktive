package com.badoo.reaktive.flowable

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.ObservableObserver
import com.badoo.reaktive.observable.subscribeSafe
import com.badoo.reaktive.scheduler.BufferedExecutor
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.utils.SimpleCondition
import com.badoo.reaktive.utils.atomicreference.AtomicReference
import com.badoo.reaktive.utils.atomicreference.getAndUpdate

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
                private val bufferedExecutor = BufferedExecutor(executor, observer::onNext)

                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onNext(value: FlowableValue<T>) {
                    bufferedExecutor.submit(value)
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