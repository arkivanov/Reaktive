package com.badoo.reaktive.scheduler

import com.badoo.reaktive.disposable.Disposable
import kotlin.time.Duration.Companion.milliseconds

fun Scheduler.submit(delayMillis: Long = 0L, task: () -> Unit): Disposable {
    val executor = newExecutor()

    executor.submit(delay = delayMillis.milliseconds) {
        task()
        executor.dispose()
    }

    return executor
}

fun Scheduler.submitRepeating(startDelayMillis: Long = 0L, periodMillis: Long, task: () -> Unit): Disposable {
    val executor = newExecutor()

    executor.submit(delay = startDelayMillis.milliseconds, period = periodMillis.milliseconds) {
        task()
        executor.dispose()
    }

    return executor
}
