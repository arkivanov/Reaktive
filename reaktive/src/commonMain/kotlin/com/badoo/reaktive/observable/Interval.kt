package com.badoo.reaktive.observable

import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.utils.atomic.AtomicLong

fun observableInterval(startDelayMillis: Long, periodMillis: Long, scheduler: Scheduler): Observable<Long> =
    observableSafe(scheduler::newExecutor) { callbacks, executor ->
        val count = AtomicLong(-1L)
        executor.submitRepeating(startDelayMillis, periodMillis) {
            callbacks.onNext(count.addAndGet(1L))
        }
    }

fun observableInterval(periodMillis: Long, scheduler: Scheduler): Observable<Long> =
    observableInterval(periodMillis, periodMillis, scheduler)