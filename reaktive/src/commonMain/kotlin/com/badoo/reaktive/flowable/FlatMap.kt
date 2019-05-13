package com.badoo.reaktive.flowable

import com.badoo.reaktive.base.ErrorCallback
import com.badoo.reaktive.base.Observer
import com.badoo.reaktive.completable.CompletableCallbacks
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.utils.SimpleCondition
import com.badoo.reaktive.utils.atomicreference.AtomicReference
import com.badoo.reaktive.utils.atomicreference.update
import com.badoo.reaktive.utils.atomicreference.updateAndGet

fun <T, R> Flowable<T>.flatMap(mapper: (T) -> Flowable<R>): Flowable<R> =
    flowableSafe {
        val disposables = CompositeDisposable()
        it.onSubscribe(disposables)
        val callbacks = BlockingFlowableCallbacks(it)
        val serializedObserver = callbacks.serialize()

        subscribeSafe(
            object : FlowableObserver<T>, ErrorCallback by serializedObserver {
                private val activeSourceCount = AtomicReference(1)

                private val mappedObserver: FlowableObserver<R> =
                    object : FlowableObserver<R>, Observer by this, CompletableCallbacks by this {
                        override fun onNext(value: FlowableValue<R>) {
                            serializedObserver.onNext(value)
                        }
                    }

                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onNext(value: FlowableValue<T>) {
                    activeSourceCount.update { it + 1 }

                    val mappedSource =
                        try {
                            mapper(value.value)
                        } catch (e: Throwable) {
                            onError(e)
                            return
                        }

                    mappedSource.subscribeSafe(mappedObserver)

                    value.onProcessed()
                }

                override fun onComplete() {
                    if (activeSourceCount.updateAndGet { it - 1 } <= 0) {
                        serializedObserver.onComplete()
                    }
                }
            }
        )
    }