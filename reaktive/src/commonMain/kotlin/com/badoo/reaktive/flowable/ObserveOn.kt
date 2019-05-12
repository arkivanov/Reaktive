package com.badoo.reaktive.flowable

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.utils.atomicreference.AtomicReference
import com.badoo.reaktive.utils.atomicreference.getAndUpdate

fun <T> Flowable<T>.observeOn(
    scheduler: Scheduler,
    backPressureStrategy: BackPressureStrategy = BackPressureStrategy.DEFAULT
): Flowable<T> =
    flowableSafe { observer ->
        val disposables = CompositeDisposable()
        observer.onSubscribe(disposables)
        val executor = scheduler.newExecutor()
        disposables += executor

        subscribeSafe(
            object : FlowableObserver<T> {
                private val state = AtomicReference<State<T>>(State(), true)

                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onNext(value: FlowableValue<T>) {
                    state
                        .getAndUpdate {
                            it.copy(
                                buffer = it.buffer + value,
                                isDraining = true
                            )
                        }
                        .takeUnless(State<*>::isDraining)
                        ?.also { drain() }
                }

                private fun drain() {
                    executor.submit {
                        while (true) {
                            val oldState =
                                state.getAndUpdate {
                                    it.copy(
                                        buffer = it.buffer.drop(1),
                                        isDraining = it.buffer.isNotEmpty()
                                    )
                                }

                            if (oldState.buffer.isEmpty()) {
                                break
                            }

                            observer.onNext(oldState.buffer[0])
                        }
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
    val buffer: List<FlowableValue<T>> = emptyList(),
    val isDraining: Boolean = false
)