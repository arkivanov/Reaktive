package com.badoo.reaktive.scheduler

import com.badoo.reaktive.looperthread.FixedWorkerStrategy

actual fun createMainScheduler(): Scheduler = SchedulerImpl(FixedWorkerStrategy(1))