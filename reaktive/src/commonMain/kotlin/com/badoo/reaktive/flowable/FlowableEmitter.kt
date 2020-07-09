package com.badoo.reaktive.flowable

import com.badoo.reaktive.base.Emitter
import com.badoo.reaktive.observable.ObservableCallbacks

interface FlowableEmitter<in T> : Emitter, ObservableCallbacks<T>
