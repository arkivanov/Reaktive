package com.badoo.reaktive.observable

import com.badoo.reaktive.scheduler.Scheduler

fun observableTimer(delayMillis: Long, scheduler: Scheduler): Observable<Long> =
    observableSafe(scheduler::newExecutor) { callbacks, executor ->
        executor.submit(delayMillis) {
            callbacks.onNext(delayMillis)
            callbacks.onComplete()
        }
    }
