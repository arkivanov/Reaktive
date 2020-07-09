package com.badoo.reaktive.flowable

import com.badoo.reaktive.base.Source

interface Flowable<out T> : Source<FlowableObserver<T>>
