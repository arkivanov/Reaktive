package com.badoo.reaktive.rxjavainterop

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.minusAssign
import com.badoo.reaktive.disposable.plusAssign
import com.badoo.reaktive.scheduler.Scheduler
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

fun io.reactivex.Scheduler.asReaktiveScheduler(): Scheduler =
    object : Scheduler {
        private val disposables = CompositeDisposable()

        override fun newExecutor(): Scheduler.Executor =
            this@asReaktiveScheduler
                .createWorker()
                .asExecutor(disposables)

        override fun destroy() {
            disposables.dispose()
            this@asReaktiveScheduler.shutdown()
        }
    }

private fun io.reactivex.Scheduler.Worker.asExecutor(disposables: CompositeDisposable): Scheduler.Executor =
    object : Scheduler.Executor {
        private val taskDisposables = CompositeDisposable()
        override val isDisposed: Boolean get() = taskDisposables.isDisposed

        init {
            disposables += this
        }

        override fun dispose() {
            taskDisposables.dispose()
            this@asExecutor.dispose()
            disposables -= this
        }

        override fun submit(startDelay: Duration, period: Duration, task: () -> Unit) {
            taskDisposables.purge()

            taskDisposables +=
                if (period.isInfinite()) {
                    this@asExecutor
                        .schedule(task, startDelay.inWholeMicroseconds, TimeUnit.MICROSECONDS)
                        .asReaktiveDisposable()
                } else {
                    this@asExecutor
                        .schedulePeriodically(task, startDelay.inWholeMicroseconds, period.inWholeMicroseconds, TimeUnit.MICROSECONDS)
                        .asReaktiveDisposable()
                }
        }

        override fun cancel() {
            taskDisposables.clear()
        }
    }
