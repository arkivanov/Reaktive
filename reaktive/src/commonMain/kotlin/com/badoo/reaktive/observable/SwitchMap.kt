package com.badoo.reaktive.observable

import com.badoo.reaktive.base.ErrorCallback
import com.badoo.reaktive.base.ValueCallback
import com.badoo.reaktive.base.subscribeSafe
import com.badoo.reaktive.base.tryCatch
import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.DisposableWrapper
import com.badoo.reaktive.disposable.plusAssign
import com.badoo.reaktive.utils.atomics.atomic
import com.badoo.reaktive.utils.atomics.change
import com.badoo.reaktive.utils.atomics.changeAndGet

fun <T, R> Observable<T>.switchMap(mapper: (T) -> Observable<R>): Observable<R> =
    observable { emitter ->
        val disposables = CompositeDisposable()
        emitter.setDisposable(disposables)

        val innerDisposableWrapper = DisposableWrapper()
        disposables += innerDisposableWrapper

        val state = atomic(SwitchMapState())
        val serializedEmitter = emitter.serialize()

        subscribe(
            object : ObservableObserver<T>, ErrorCallback by serializedEmitter {
                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }

                override fun onNext(value: T) {
                    serializedEmitter.tryCatch(
                        block = { mapper(value) },
                        onSuccess = ::onInnerObservable
                    )
                }

                private fun onInnerObservable(observable: Observable<R>) {
                    val localDisposableWrapper = DisposableWrapper()

                    /*
                     * Dispose any existing inner Observable.
                     * If a previous Observable did not provide its disposable yet
                     * it will be disposed automatically later since
                     * its localDisposableWrapper is disposed.
                     */
                    innerDisposableWrapper.set(localDisposableWrapper)

                    val innerObserver =
                        object : ObservableObserver<R>, ValueCallback<R> by serializedEmitter,
                            ErrorCallback by serializedEmitter {
                            override fun onSubscribe(disposable: Disposable) {
                                localDisposableWrapper.set(disposable)
                            }

                            override fun onComplete() {
                                val actualState = state
                                    .changeAndGet { previousState ->
                                        if (previousState.innerObserver == this) {
                                            previousState.copy(innerObserver = null)
                                        } else {
                                            previousState
                                        }
                                    }
                                checkStateFinished(actualState)
                            }
                        }

                    state.change { previousState ->
                        previousState.copy(innerObserver = innerObserver)
                    }
                    observable.subscribeSafe(innerObserver)
                }

                override fun onComplete() {
                    val actualState =
                        state.changeAndGet { previousState -> previousState.copy(isUpstreamCompleted = true) }
                    checkStateFinished(actualState)
                }

                private fun checkStateFinished(state: SwitchMapState) {
                    if (state.isFinished) {
                        serializedEmitter.onComplete()
                    }
                }
            }
        )
    }

private data class SwitchMapState(
    val isUpstreamCompleted: Boolean = false,
    val innerObserver: Any? = null
) {
    val isFinished: Boolean get() = isUpstreamCompleted && (innerObserver == null)
}

fun <T, U, R> Observable<T>.switchMap(
    mapper: (T) -> Observable<U>,
    resultSelector: (T, U) -> R
): Observable<R> =
    switchMap { t ->
        mapper(t).map { u -> resultSelector(t, u) }
    }
