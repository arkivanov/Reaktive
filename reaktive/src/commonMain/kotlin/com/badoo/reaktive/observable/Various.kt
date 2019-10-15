package com.badoo.reaktive.observable

import com.badoo.reaktive.base.tryCatch
import com.badoo.reaktive.disposable.Disposable

inline fun <T> observableUnsafe(crossinline onSubscribe: (observer: ObservableObserver<T>) -> Unit): Observable<T> =
    object : Observable<T> {
        override fun subscribe(observer: ObservableObserver<T>) {
            onSubscribe(observer)
        }
    }

internal inline fun <T, D : Disposable> observableUnsafe(
    crossinline disposableFactory: () -> D,
    crossinline block: (observer: ObservableObserver<T>, disposable: D) -> Unit
): Observable<T> =
    observableUnsafe { observer ->
        val disposable = disposableFactory()
        observer.onSubscribe(disposable)

        if (!disposable.isDisposed) {
            block(observer, disposable)
        }
    }

fun <T> observableOf(value: T): Observable<T> =
    observableUnsafe(::Disposable) { observer, disposable ->
        observer.onNext(value)
        if (!disposable.isDisposed) {
            observer.onComplete()
        }
    }

fun <T> T.toObservable(): Observable<T> = observableOf(this)

fun <T> Iterable<T>.asObservable(): Observable<T> =
    observableUnsafe(::Disposable) { observer, disposable ->
        forEach {
            observer.onNext(it)
            if (disposable.isDisposed) {
                return@observableUnsafe
            }
        }

        observer.onComplete()
    }

fun <T> observableOf(vararg values: T): Observable<T> =
    observableUnsafe(::Disposable) { observer, disposable ->
        values.forEach {
            observer.onNext(it)
            if (disposable.isDisposed) {
                return@observableUnsafe
            }
        }

        observer.onComplete()
    }

fun <T> observableOfError(error: Throwable): Observable<T> =
    observableUnsafe(::Disposable) { observer, _ ->
        observer.onError(error)
    }

fun <T> Throwable.toObservableOfError(): Observable<T> = observableOfError(this)

fun <T> observableOfEmpty(): Observable<T> =
    observableUnsafe(::Disposable) { observer, _ -> observer.onComplete() }

fun <T> observableOfNever(): Observable<T> = observableUnsafe(::Disposable) { _, _ -> }

fun <T> observableFromFunction(func: () -> T): Observable<T> =
    observableUnsafe(::Disposable) { observer, disposable ->
        observer.tryCatch(block = func) {
            if (!disposable.isDisposed) {
                return@observableUnsafe
            }

            observer.onNext(it)

            if (disposable.isDisposed) {
                return@observableUnsafe
            }

            observer.onComplete()
        }
    }
