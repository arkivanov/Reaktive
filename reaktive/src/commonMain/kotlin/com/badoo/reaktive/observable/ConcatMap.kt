package com.badoo.reaktive.observable

import com.badoo.reaktive.base.ErrorCallback
import com.badoo.reaktive.base.Observer
import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.base.tryCatch
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.utils.atomic.AtomicReference
import com.badoo.reaktive.utils.atomic.getAndUpdate
import com.badoo.reaktive.utils.atomic.updateAndGet

fun <T, R> Observable<T>.concatMap(mapper: (T) -> Observable<R>): Observable<R> =
    observableSafe(::CompositeDisposable) { callbacks, disposables ->
        val serializedCallbacks = callbacks.serialize()

        val state = AtomicReference(ConcatMapState<T>())

        subscribeSafe(
            object : ObservableObserver<T>, ErrorCallback by serializedCallbacks {
                val mappedObserver =
                    object : ObservableObserver<R>, Observer by this, ObservableCallbacks<R> by serializedCallbacks {
                        override fun onComplete() {
                            val oldState =
                                state.getAndUpdate {
                                    it.copy(
                                        queue = it.queue.drop(1),
                                        isMappedSourceActive = it.queue.isNotEmpty()
                                    )
                                }

                            if (oldState.queue.isNotEmpty()) {
                                mapAndSubscribe(oldState.queue[0])
                            } else if (oldState.isUpstreamCompleted) {
                                serializedCallbacks.onComplete()
                            }
                        }
                    }


                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onNext(value: T) {
                    val oldState =
                        state.getAndUpdate {
                            it.copy(
                                queue = if (it.isMappedSourceActive) it.queue + value else it.queue,
                                isMappedSourceActive = true
                            )
                        }

                    if (!oldState.isMappedSourceActive) {
                        mapAndSubscribe(value)
                    }
                }

                override fun onComplete() {
                    val newState =
                        state.updateAndGet {
                            it.copy(isUpstreamCompleted = true)
                        }

                    if (newState.isUpstreamCompleted && !newState.isMappedSourceActive) {
                        serializedCallbacks.onComplete()
                    }
                }

                private fun mapAndSubscribe(value: T) {
                    serializedCallbacks.tryCatch({ mapper(value) }) {
                        it.subscribeSafe(mappedObserver)
                    }
                }
            }
        )
    }

private data class ConcatMapState<T>(
    val queue: List<T> = emptyList(),
    val isMappedSourceActive: Boolean = false,
    val isUpstreamCompleted: Boolean = false
)