package com.badoo.reaktive.flowable

interface FlowableValueCallback<in T> {

    fun onNext(value: T): FlowableAwait?
}
