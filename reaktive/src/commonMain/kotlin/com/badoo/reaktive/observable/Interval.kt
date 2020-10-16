package com.badoo.reaktive.observable

import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.utils.atomics.addAndGet
import com.badoo.reaktive.utils.atomics.atomic

fun observableInterval(startDelayMillis: Long, periodMillis: Long, scheduler: Scheduler): Observable<Long> =
    observable { emitter ->
        val executor = scheduler.newExecutor()
        emitter.setDisposable(executor)

        val count = atomic(-1L)
        executor.submitRepeating(startDelayMillis, periodMillis) {
            emitter.onNext(count.addAndGet(1L))
        }
    }

fun observableInterval(periodMillis: Long, scheduler: Scheduler): Observable<Long> =
    observableInterval(periodMillis, periodMillis, scheduler)
