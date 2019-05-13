package com.badoo.reaktive.flowable

import com.badoo.reaktive.base.Emitter

interface FlowableEmitter<in T> : Emitter, FlowableCallbacks<T>