package com.badoo.reaktive.single

import com.badoo.reaktive.base.tryCatch
import com.badoo.reaktive.disposable.Disposable

inline fun <T> singleUnsafe(crossinline onSubscribe: (observer: SingleObserver<T>) -> Unit): Single<T> =
    object : Single<T> {
        override fun subscribe(observer: SingleObserver<T>) {
            onSubscribe(observer)
        }
    }

internal inline fun <T, D : Disposable> singleUnsafe(
    crossinline disposableFactory: () -> D,
    crossinline block: (observer: SingleObserver<T>, disposable: D) -> Unit
): Single<T> =
    singleUnsafe { observer ->
        val disposable = disposableFactory()
        observer.onSubscribe(disposable)

        if (!disposable.isDisposed) {
            block(observer, disposable)
        }
    }

fun <T> singleOf(value: T): Single<T> =
    singleUnsafe(::Disposable) { observer, _ ->
        observer.onSuccess(value)
    }

fun <T> T.toSingle(): Single<T> = singleOf(this)

fun <T> singleOfNever(): Single<T> = singleUnsafe(::Disposable) { _, _ -> }

fun <T> singleOfError(error: Throwable): Single<T> =
    singleUnsafe(::Disposable) { observer, _ ->
        observer.onError(error)
    }

fun <T> Throwable.toSingleOfError(): Single<T> = singleOfError(this)

fun <T> singleFromFunction(func: () -> T): Single<T> =
    singleUnsafe(::Disposable) { observer, disposable ->
        observer.tryCatch(block = func) {
            if (!disposable.isDisposed) {
                observer.onSuccess(it)
            }
        }
    }

fun <T> (() -> T).asSingle(): Single<T> = singleFromFunction(this)
