package com.badoo.reaktive.completable

import com.badoo.reaktive.scheduler.Scheduler

fun completableTimer(delayMillis: Long, scheduler: Scheduler): Completable =
    completableSafe(scheduler::newExecutor) { callbacks, executor ->
        executor.submit(delayMillis, callbacks::onComplete)
    }