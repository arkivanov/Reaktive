package com.badoo.reaktive.flowable

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.plusAssign
import com.badoo.reaktive.scheduler.BufferedExecutor
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.utils.freeze

inline fun <T> flowableUnsafe(crossinline onSubscribe: (observer: FlowableObserver<T>) -> Unit): Flowable<T> =
    object : Flowable<T> {
        override fun subscribe(observer: FlowableObserver<T>) {
            onSubscribe(observer)
        }
    }

fun <T> flowable(onSubscribe: (FlowableEmitter<T>) -> Unit): Flowable<T> =
    flowableUnsafe { observer ->
        val emitter =
            object : FlowableEmitter<T> {
                override val isDisposed: Boolean
                    get() = TODO()

                override fun setDisposable(disposable: Disposable?) {
                    TODO("Not yet implemented")
                }

                override fun onNext(value: T) {
                    observer.onNext(value)?.await()
                }

                override fun onComplete() {
                    TODO("Not yet implemented")
                }

                override fun onError(error: Throwable) {
                    TODO("Not yet implemented")
                }
            }

        onSubscribe(emitter)
    }

fun <T> Flowable<T>.observeOn(scheduler: Scheduler): Flowable<T> =
    flowable { emitter ->
        val disposables = CompositeDisposable()
        emitter.setDisposable(disposables)
        val executor = scheduler.newExecutor()
        disposables += executor

        subscribe(
            object : FlowableObserver<T> {
                private val awaitNotify = FlowableAwaitNotify<T>()
                private val bufferedExecutor = BufferedExecutor<FlowableValue<T>>(executor) { it.consume(emitter::onNext) }

                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onNext(value: T): FlowableAwait {
                    awaitNotify.value = value
                    bufferedExecutor.submit(awaitNotify)

                    return awaitNotify
                }

                override fun onComplete() {
                    executor.submit {
                        emitter.onComplete()
                    }
                }

                override fun onError(error: Throwable) {
                    error.freeze()

                    executor.submit {
                        emitter.onError(error)
                    }
                }
            }
        )
    }

fun <T> Flowable<T>.subscribe(onNext: (T) -> Unit): Disposable {

    subscribe(
        object : FlowableObserver<T> {
            override fun onSubscribe(disposable: Disposable) {
                TODO("Not yet implemented")
            }

            override fun onNext(value: T): FlowableAwait? {
                onNext(value)

                return null
            }

            override fun onComplete() {
                TODO("Not yet implemented")
            }

            override fun onError(error: Throwable) {
                TODO("Not yet implemented")
            }
        }
    )

}
