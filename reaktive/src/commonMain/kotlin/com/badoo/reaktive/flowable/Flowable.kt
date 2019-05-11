package com.badoo.reaktive.flowable

interface Flowable<out T> {

    fun subscribe(observer: FlowableObserver<T>)
}