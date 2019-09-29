package com.badoo.reaktive.looperthread

import kotlin.native.concurrent.Worker

internal interface WorkerStrategy {

    fun get(): Worker

    fun recycle(worker: Worker)

    fun destroy()
}