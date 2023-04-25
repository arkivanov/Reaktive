package com.badoo.reaktive.scheduler

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.minusAssign
import com.badoo.reaktive.disposable.plusAssign
import com.badoo.reaktive.looperthread.LooperThreadStrategy
import com.badoo.reaktive.utils.clock.Clock
import com.badoo.reaktive.utils.clock.DefaultClock
import kotlin.native.concurrent.AtomicInt
import kotlin.time.Duration

internal class SchedulerImpl(
    private val looperThreadStrategy: LooperThreadStrategy,
    private val clock: Clock = DefaultClock,
) : Scheduler {

    private val disposables = CompositeDisposable()

    override fun newExecutor(): Scheduler.Executor = ExecutorImpl(disposables, looperThreadStrategy, clock)

    override fun destroy() {
        disposables.dispose()
        looperThreadStrategy.destroy()
    }

    private class ExecutorImpl(
        private val disposables: CompositeDisposable,
        private val looperThreadStrategy: LooperThreadStrategy,
        private val clock: Clock,
    ) : Scheduler.Executor {

        private val looperThread = looperThreadStrategy.get()
        private val _isDisposed = AtomicInt(0)
        override val isDisposed: Boolean get() = _isDisposed.value != 0

        init {
            disposables += this
        }

        override fun dispose() {
            _isDisposed.value = 1
            cancel()
            looperThreadStrategy.recycle(looperThread)
            disposables -= this
        }

        override fun submit(startDelay: Duration, period: Duration, task: () -> Unit) {
            if (isDisposed) {
                return
            }

            if (period.isInfinite()) {
                looperThread.schedule(this, getStartTime(startDelay), task)
                return
            }

            lateinit var t: () -> Unit
            t = {
                if (!isDisposed) {
                    val nextStartTime = getStartTime(period)
                    task()
                    if (!isDisposed) {
                        looperThread.schedule(this, nextStartTime, t)
                    }
                }
            }

            looperThread.schedule(this, getStartTime(startDelay), t)
        }

        override fun cancel() {
            looperThread.cancel(this)
        }

        private fun getStartTime(delay: Duration): Duration =
            clock.uptime + delay
    }
}
