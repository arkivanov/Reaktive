package com.badoo.reaktive.flowable

inline fun <T> flowableUnsafe(crossinline onSubscribe: (observer: FlowableObserver<T>) -> Unit): Flowable<T> =
    object : Flowable<T> {
        override fun subscribe(observer: FlowableObserver<T>) {
            onSubscribe(observer)
        }
    }

inline fun <T> flowableSafe(crossinline onSubscribe: (observer: FlowableObserver<T>) -> Unit): Flowable<T> =
    flowableUnsafe {
        onSubscribe(it.safe())
    }
