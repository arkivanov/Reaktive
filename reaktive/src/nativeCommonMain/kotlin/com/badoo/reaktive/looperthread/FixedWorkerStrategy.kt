package com.badoo.reaktive.looperthread

import kotlin.native.concurrent.AtomicInt
import kotlin.native.concurrent.Worker

internal class FixedWorkerStrategy(threadCount: Int) : WorkerStrategy {

    private val pool = List(threadCount) { Worker.start() }
    private val threadIndex = AtomicInt(-1)

    override fun get(): Worker = pool[threadIndex.addAndGet(1) % pool.size]

    override fun recycle(worker: Worker) {
        // no-op
    }

    override fun destroy() {
        pool.forEach { it.requestTermination() }
    }
}