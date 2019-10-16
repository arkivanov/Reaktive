package com.badoo.reaktive.single

import com.badoo.reaktive.scheduler.Scheduler

fun singleTimer(delayMillis: Long, scheduler: Scheduler): Single<Long> =
    singleSafe(scheduler::newExecutor) { callbacks, executor ->
        executor.submit(delayMillis) { callbacks.onSuccess(delayMillis) }
    }