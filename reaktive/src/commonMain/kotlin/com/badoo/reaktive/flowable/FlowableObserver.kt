package com.badoo.reaktive.flowable

import com.badoo.reaktive.base.Observer
import com.badoo.reaktive.completable.CompletableCallbacks

interface FlowableObserver<in T> : Observer, CompletableCallbacks {

    fun onNext(value: FlowableValue<T>)
}