package com.badoo.reaktive.observable

import com.badoo.reaktive.base.ErrorCallback
import com.badoo.reaktive.base.Observer
import com.badoo.reaktive.base.ValueCallback
import com.badoo.reaktive.base.tryCatch
import com.badoo.reaktive.completable.CompletableCallbacks
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.MaybeObserver

/**
 * Returns an [Observable] that calls the [handler] when this [Observable] completes,
 * subscribes to the returned [Maybe], and resubscribes to this [Observable] when the
 * [Maybe] succeeds with any value. If the returned [Maybe] completes then the returned
 * [Observable] completes as well.
 *
 * Please refer to the corresponding RxJava [document](http://reactivex.io/RxJava/javadoc/io/reactivex/Observable.html#repeatWhen-io.reactivex.functions.Function-).
 */
fun <T> Observable<T>.repeatWhen(handler: (attempt: Int) -> Maybe<*>): Observable<T> =
    observable { emitter ->
        val observer =
            object : ObservableObserver<T>, ValueCallback<T> by emitter, ErrorCallback by emitter {
                private var repeatNumber = 0
                private var recursiveGuard = 0

                private val repeatObserver: MaybeObserver<Any?> =
                    object : MaybeObserver<Any?>, Observer by this, CompletableCallbacks by emitter {
                        override fun onSuccess(value: Any?) {
                            emitter.tryCatch(block = ::subscribeToUpstream)
                        }
                    }

                override fun onSubscribe(disposable: Disposable) {
                    emitter.setDisposable(disposable)
                }

                override fun onComplete() {
                    emitter.tryCatch {
                        repeatNumber++
                        handler(repeatNumber).subscribe(repeatObserver)
                    }
                }

                fun subscribeToUpstream() {
                    // Prevents recursive subscriptions
                    if (++recursiveGuard == 1) {
                        do {
                            subscribe(this)
                        } while (--recursiveGuard > 0)
                    }
                }
            }

        observer.subscribeToUpstream()
    }
