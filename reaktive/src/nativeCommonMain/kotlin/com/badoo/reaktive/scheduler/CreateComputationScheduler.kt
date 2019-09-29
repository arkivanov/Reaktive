package com.badoo.reaktive.scheduler

import com.badoo.reaktive.looperthread.FixedWorkerStrategy
import kotlin.math.max
import platform.posix._SC_NPROCESSORS_ONLN
import platform.posix.sysconf

actual fun createComputationScheduler(): Scheduler = SchedulerImpl(FixedWorkerStrategy(threadCount))

private val threadCount: Int get() = max(2, sysconf(_SC_NPROCESSORS_ONLN).toInt())