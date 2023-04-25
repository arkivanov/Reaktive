package com.badoo.reaktive.completable

import com.badoo.reaktive.scheduler.Scheduler
import kotlin.time.Duration.Companion.milliseconds

/**
 * Signals `onComplete` after the given [delayMillis] delay.
 *
 * Please refer to the corresponding RxJava [document](http://reactivex.io/RxJava/javadoc/io/reactivex/Completable.html#timer-long-java.util.concurrent.TimeUnit-io.reactivex.Scheduler-).
 */
fun completableTimer(delayMillis: Long, scheduler: Scheduler): Completable =
    completable { emitter ->
        val executor = scheduler.newExecutor()
        emitter.setDisposable(executor)
        executor.submit(delay = delayMillis.milliseconds, task = emitter::onComplete)
    }
