package com.badoo.reaktive.observable

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.plusAssign
import com.badoo.reaktive.scheduler.BufferedExecutor
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.utils.freeze

fun <T> Observable<T>.observeOn(
    scheduler: Scheduler,
    bufferSize: Int = Int.MAX_VALUE,
    bufferOverflowStrategy: BufferOverflowStrategy = BufferOverflowStrategy.ERROR
): Observable<T> =
    observable { emitter ->
        val disposables = CompositeDisposable()
        emitter.setDisposable(disposables)
        val executor = scheduler.newExecutor()
        disposables += executor
        val bufferedExecutor = BufferedExecutor(executor, bufferSize, bufferOverflowStrategy, emitter::onNext)
        disposables += bufferedExecutor

        subscribe(
            object : ObservableObserver<T> {
                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onNext(value: T) {
                    try {
                        bufferedExecutor.submit(value)
                    } catch (e: Throwable) {
                        emitter.onError(e)
                    }
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

enum class BufferOverflowStrategy {

    ERROR,
    DROP_OLDEST,
    DROP_NEWEST,
    BLOCK
}

class BufferOverflowException : Exception()
