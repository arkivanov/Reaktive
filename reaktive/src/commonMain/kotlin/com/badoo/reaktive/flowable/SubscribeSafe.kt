package com.badoo.reaktive.flowable

import com.badoo.reaktive.utils.handleSourceError

internal fun <T> Flowable<T>.subscribeSafe(observer: FlowableObserver<T>) {
    try {
        subscribe(observer)
    } catch (e: Throwable) {
        handleSourceError(e, observer::onError)
    }
}