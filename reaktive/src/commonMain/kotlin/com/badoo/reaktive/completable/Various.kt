package com.badoo.reaktive.completable

import com.badoo.reaktive.base.tryCatch
import com.badoo.reaktive.disposable.Disposable

inline fun completableUnsafe(crossinline onSubscribe: (observer: CompletableObserver) -> Unit): Completable =
    object : Completable {
        override fun subscribe(observer: CompletableObserver) {
            onSubscribe(observer)
        }
    }

internal inline fun <D : Disposable> completableUnsafe(
    crossinline disposableFactory: () -> D,
    crossinline block: (observer: CompletableObserver, disposable: D) -> Unit
): Completable =
    completableUnsafe { observer ->
        val disposable = disposableFactory()
        observer.onSubscribe(disposable)

        if (!disposable.isDisposed) {
            block(observer, disposable)
        }
    }

fun completableOfError(error: Throwable): Completable =
    completableUnsafe(::Disposable) { observer, _ ->
        observer.onError(error)
    }

fun Throwable.toCompletableOfError(): Completable = completableOfError(this)

fun completableOfEmpty(): Completable =
    completableUnsafe(::Disposable) { observer, _ ->
        observer.onComplete()
    }

fun completableOfNever(): Completable = completableUnsafe(::Disposable) { _, _ -> }

fun completableFromFunction(func: () -> Unit): Completable =
    completableUnsafe(::Disposable) { observer, disposable ->
        observer.tryCatch(block = func) {
            if (!disposable.isDisposed) {
                observer.onComplete()
            }
        }
    }
