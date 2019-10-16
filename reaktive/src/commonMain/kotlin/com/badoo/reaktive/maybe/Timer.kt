package com.badoo.reaktive.maybe

import com.badoo.reaktive.scheduler.Scheduler

fun maybeTimer(delayMillis: Long, scheduler: Scheduler): Maybe<Long> =
    maybeSafe(scheduler::newExecutor) { emitter, executor ->
        executor.submit(delayMillis) { emitter.onSuccess(delayMillis) }
    }