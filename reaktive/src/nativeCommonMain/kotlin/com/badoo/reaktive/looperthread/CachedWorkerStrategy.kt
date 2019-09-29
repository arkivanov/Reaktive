package com.badoo.reaktive.looperthread

import com.badoo.reaktive.utils.ExpirationPool
import kotlin.native.concurrent.Worker

internal class CachedWorkerStrategy(
    private val keepAliveTimeoutMillis: Long
) : WorkerStrategy {

    override fun get(): Worker = pool.acquire() ?: Worker.start()

    override fun recycle(worker: Worker) {
        pool.release(worker, keepAliveTimeoutMillis)
    }

    override fun destroy() {
        // no-op
    }

    private companion object {
        private val pool by lazy {
            ExpirationPool<Worker> { it.requestTermination(processScheduledJobs = false) }
        }
    }
}