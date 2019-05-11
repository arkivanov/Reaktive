package com.badoo.reaktive.flowable

import com.badoo.reaktive.base.Emitter
import com.badoo.reaktive.completable.CompletableCallbacks

interface FlowableEmitter<in T> : Emitter, CompletableCallbacks {

    fun onNext(value: T)
}