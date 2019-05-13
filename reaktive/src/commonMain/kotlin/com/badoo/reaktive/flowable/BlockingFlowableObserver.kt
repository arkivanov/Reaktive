package com.badoo.reaktive.flowable

import com.badoo.reaktive.base.Observer
import com.badoo.reaktive.completable.CompletableCallbacks
import com.badoo.reaktive.utils.SimpleCondition
import com.badoo.reaktive.utils.atomicreference.AtomicReference

internal class BlockingFlowableCallbacks<T>(
    private val delegate: FlowableObserver<T>
) : FlowableCallbacks<T>, Observer by delegate, CompletableCallbacks by delegate {

    private val valueRef = AtomicReference<T?>(null, true)
    private val condition = SimpleCondition()

    private val flowableValue =
        object : FlowableValue<T> {
            override val value: T get() = valueRef.value!!

            override fun onProcessed() {
                condition.signal()
            }
        }

    override fun onNext(value: T) {
        valueRef.value = value
        condition.reset()
        delegate.onNext(flowableValue)
        condition.await()
    }
}