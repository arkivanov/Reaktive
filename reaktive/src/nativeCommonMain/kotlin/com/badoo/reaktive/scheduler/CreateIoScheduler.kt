package com.badoo.reaktive.scheduler

import com.badoo.reaktive.looperthread.CachedWorkerStrategy

actual fun createIoScheduler(): Scheduler = SchedulerImpl(CachedWorkerStrategy(keepAliveTimeoutMillis = 60000L))