package com.badoo.reaktive.flowable

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.scheduler.Scheduler
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
                private val state = AtomicReference<State<T>>(State(), true)

                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onNext(value: FlowableValue<T>) {
                    try {
                        state
                            .getAndUpdate {
                                it.addItem(value.value) ?: return
                            }
                            .isDraining
                            .takeUnless { it }
                            ?.also { drain() }
                    } finally {
                        value.onProcessed()
                    }
                }

                private fun <T> State<T>.addItem(item: T): State<T>? {
                    val newBuffer =
                        if (!isDraining || (buffer.size < backPressureStrategy.bufferSize)) {
                            buffer + item
                        } else {
                            when (backPressureStrategy.overflowStrategy) {
                                BackPressureStrategy.OverflowStrategy.DROP_OLDEST -> {
                                    val oldList = buffer
                                    val newList = ArrayList<T>(buffer.size)
                                    for (i in 1 until oldList.size) {
                                        newList += oldList[i]
                                    }
                                    newList += item
                                    newList
                                }

                                BackPressureStrategy.OverflowStrategy.DROP_LATEST -> buffer

                                BackPressureStrategy.OverflowStrategy.ERROR -> {
                                    observer.onError(MissingBackPressureException())
                                    return null
                                }
                            }
                        }

                    return copy(buffer = newBuffer, isDraining = true)
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

                            observer.onNextBlocking(oldState.buffer[0])
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
    val buffer: List<T> = emptyList(),
    val isDraining: Boolean = false
)