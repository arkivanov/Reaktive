package com.badoo.reaktive.maybe

import com.badoo.reaktive.base.tryCatch
import com.badoo.reaktive.disposable.Disposable

inline fun <T> maybeUnsafe(crossinline onSubscribe: (observer: MaybeObserver<T>) -> Unit): Maybe<T> =
    object : Maybe<T> {
        override fun subscribe(observer: MaybeObserver<T>) {
            onSubscribe(observer)
        }
    }

internal inline fun <T, D : Disposable> maybeUnsafe(
    crossinline disposableFactory: () -> D,
    crossinline block: (observer: MaybeObserver<T>, disposable: D) -> Unit
): Maybe<T> =
    maybeUnsafe { observer ->
        val disposable = disposableFactory()
        observer.onSubscribe(disposable)

        if (!disposable.isDisposed) {
            block(observer, disposable)
        }
    }

fun <T> maybeOf(value: T): Maybe<T> =
    maybeUnsafe(::Disposable) { observer, _ ->
        observer.onSuccess(value)
    }

fun <T> T.toMaybe(): Maybe<T> = maybeOf(this)

fun <T : Any> maybeOfNotNull(value: T?): Maybe<T> =
    maybeUnsafe(::Disposable) { observer, _ ->
        if (value == null) {
            observer.onComplete()
        } else {
            observer.onSuccess(value)
        }
    }

fun <T : Any> T?.toMaybeNotNull(): Maybe<T> = maybeOfNotNull(this)

fun <T> maybeOfError(error: Throwable): Maybe<T> =
    maybeUnsafe(::Disposable) { observer, _ ->
        observer.onError(error)
    }

fun <T> Throwable.toMaybeOfError(): Maybe<T> = maybeOfError(this)

fun <T> maybeOfEmpty(): Maybe<T> =
    maybeUnsafe(::Disposable) { observer, _ ->
        observer.onComplete()
    }

fun <T> maybeOfNever(): Maybe<T> = maybe {}

fun <T> maybeFromFunction(func: () -> T): Maybe<T> =
    maybeUnsafe(::Disposable) { observer, disposable ->
        observer.tryCatch(block = func) {
            if (!disposable.isDisposed) {
                observer.onSuccess(it)
            }
        }
    }
