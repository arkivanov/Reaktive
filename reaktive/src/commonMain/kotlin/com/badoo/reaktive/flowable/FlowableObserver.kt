package com.badoo.reaktive.flowable

import com.badoo.reaktive.base.Observer

interface FlowableObserver<in T> : Observer, FlowableCallbacks<FlowableValue<T>>