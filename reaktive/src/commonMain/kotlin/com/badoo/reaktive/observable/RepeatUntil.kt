package com.badoo.reaktive.observable

import com.badoo.reaktive.base.ErrorCallback
import com.badoo.reaktive.base.ValueCallback
import com.badoo.reaktive.base.tryCatch
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.utils.atomics.addAndGet
import com.badoo.reaktive.utils.atomics.atomic

fun <T> Observable<T>.repeatUntil(predicate: () -> Boolean): Observable<T> =
    observable { emitter ->
        val observer =
            object : ObservableObserver<T>, ValueCallback<T> by emitter, ErrorCallback by emitter {
                private val recursiveGuard = atomic(0)

                override fun onSubscribe(disposable: Disposable) {
                    emitter.setDisposable(disposable)
                }

                override fun onComplete() {
                    emitter.tryCatch(
                        block = predicate,
                        onSuccess = {
                            if (!emitter.isDisposed) {
                                if (it) {
                                    emitter.onComplete()
                                } else {
                                    subscribeToUpstream()
                                }
                            }
                        }
                    )
                }

                fun subscribeToUpstream() {
                    // Prevents recursive subscriptions
                    if (recursiveGuard.addAndGet(1) == 1) {
                        do {
                            subscribe(this)
                        } while (recursiveGuard.addAndGet(-1) > 0)
                    }
                }
            }

        observer.subscribeToUpstream()
    }
