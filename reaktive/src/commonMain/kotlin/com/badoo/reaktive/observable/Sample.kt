package com.badoo.reaktive.observable

import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.utils.atomic.AtomicReference

fun <T> Observable<T>.sample(windowMillis: Long, scheduler: Scheduler): Observable<T> =
    observableSafe(::CompositeDisposable) { callbacks, disposables ->
        val executor = scheduler.newExecutor()
        disposables += executor

        subscribeSafe(
            object : ObservableObserver<T> {
                private val lastValue = AtomicReference<SampleLastValue<T>?>(null)

                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable

                    executor.submitRepeating(periodMillis = windowMillis) {
                        lastValue
                            .value
                            ?.value
                            ?.also(callbacks::onNext)
                    }
                }

                override fun onNext(value: T) {
                    lastValue.value = SampleLastValue(value)
                }

                override fun onComplete() {
                    executor.cancel()
                    executor.submit(task = callbacks::onComplete)
                }

                override fun onError(error: Throwable) {
                    executor.cancel()
                    executor.submit { callbacks.onError(error) }
                }
            }
        )
    }

private class SampleLastValue<T>(
    val value: T
)