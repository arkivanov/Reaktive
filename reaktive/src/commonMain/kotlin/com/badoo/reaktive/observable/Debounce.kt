@file:Suppress("MatchingDeclarationName")

package com.badoo.reaktive.observable

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.plusAssign
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.utils.atomic.AtomicReference
import com.badoo.reaktive.utils.atomic.change
import com.badoo.reaktive.utils.atomic.getAndChange
import kotlin.time.Duration

/**
 * Returns an [Observable] that mirrors the source [Observable], but drops elements
 * that are followed by newer ones before the [timeout] expires on a specified [Scheduler].
 *
 * Please refer to the corresponding RxJava [document](http://reactivex.io/RxJava/javadoc/io/reactivex/Observable.html#debounce-long-java.util.concurrent.TimeUnit-io.reactivex.Scheduler-).
 */
fun <T> Observable<T>.debounce(timeout: Duration, scheduler: Scheduler): Observable<T> =
    observable { emitter ->
        val disposables = CompositeDisposable()
        emitter.setDisposable(disposables)
        val executor = scheduler.newExecutor()
        disposables += executor

        subscribe(
            object : ObservableObserver<T> {
                private val pendingValue = AtomicReference<DebouncePendingValue<T>?>(null)

                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onNext(value: T) {
                    val newPendingValue = DebouncePendingValue(value)
                    pendingValue.value = newPendingValue

                    executor.cancel()

                    executor.submit(delay = timeout) {
                        pendingValue.change {
                            if (it === newPendingValue) null else it
                        }

                        emitter.onNext(value)
                    }
                }

                override fun onComplete() {
                    executor.cancel()

                    executor.submit {
                        pendingValue.getAndChange { null }?.let { emitter.onNext(it.value) }
                        emitter.onComplete()
                    }
                }

                override fun onError(error: Throwable) {
                    executor.cancel()

                    executor.submit {
                        emitter.onError(error)
                    }
                }
            }
        )
    }

internal class DebouncePendingValue<out T>(
    val value: T
)
