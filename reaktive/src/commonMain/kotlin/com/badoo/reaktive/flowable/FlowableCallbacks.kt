package com.badoo.reaktive.flowable

import com.badoo.reaktive.completable.CompletableCallbacks

interface FlowableCallbacks<in T> : CompletableCallbacks {

    fun onNext(value: T)
}