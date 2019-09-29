package com.badoo.reaktive.scheduler

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.looperthread.WorkerStrategy
import kotlin.math.max
import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.freeze
import kotlin.system.measureTimeMicros

internal class SchedulerImpl(
    private val workerStrategy: WorkerStrategy
) : Scheduler {

    private val disposables = CompositeDisposable()

    override fun newExecutor(): Scheduler.Executor =
        ExecutorImpl(workerStrategy)
            .also(disposables::add)

    override fun destroy() {
        disposables.dispose()
        workerStrategy.destroy()
    }

    private class ExecutorImpl(
        private val workerStrategy: WorkerStrategy
    ) : Scheduler.Executor {

        private val worker = workerStrategy.get()
        private val tasks = CompositeDisposable()
        override val isDisposed: Boolean get() = tasks.isDisposed

        override fun dispose() {
            tasks.dispose()
            workerStrategy.recycle(worker)
        }

        override fun submit(delayMillis: Long, task: () -> Unit) {
            if (!isDisposed) {
                submitTask(delayMillis * 1000L, task)
            }
        }

        override fun submitRepeating(startDelayMillis: Long, periodMillis: Long, task: () -> Unit) {
            lateinit var t: () -> Unit
            t = {
                if (!isDisposed) {
                    val taskDurationMicros = measureTimeMicros(task)
                    if (!isDisposed) {
                        submitTask(max(startDelayMillis * 1000L - taskDurationMicros, 0L), t)
                    }
                }
            }
            submit(startDelayMillis, t)
        }

        override fun cancel() {
            tasks.clear()
        }

        /*
         * There is no way to cancel a particular scheduled Worker task at the moment.
         * The workaround is to decouple the original task with AtomicReference and
         * clear it when cancelled.
         */
        private fun submitTask(startDelayMicros: Long, task: () -> Unit) {
            val taskWrapper = TaskWrapper(AtomicReference(task.freeze()))
            tasks += taskWrapper
            worker.executeAfter(startDelayMicros, taskWrapper.freeze())
        }

        private class TaskWrapper(
            private val delegate: AtomicReference<(() -> Unit)?>
        ) : Disposable, () -> Unit {
            override val isDisposed: Boolean get() = delegate.value == null

            override fun dispose() {
                delegate.value = null
            }

            override fun invoke() {
                val task: (() -> Unit)? = delegate.value
                delegate.value = null
                task?.invoke()
            }
        }
    }
}