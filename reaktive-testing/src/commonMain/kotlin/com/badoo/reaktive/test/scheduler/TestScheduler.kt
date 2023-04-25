package com.badoo.reaktive.test.scheduler

import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.scheduler.Scheduler.Executor
import com.badoo.reaktive.utils.atomic.AtomicBoolean
import com.badoo.reaktive.utils.atomic.AtomicLong
import com.badoo.reaktive.utils.atomic.AtomicReference
import com.badoo.reaktive.utils.atomic.change
import com.badoo.reaktive.utils.atomic.getAndChange
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class TestScheduler(
    isManualProcessing: Boolean = false
) : Scheduler {

    private val _isManualProcessing = AtomicBoolean(isManualProcessing)
    var isManualProcessing: Boolean
        get() = _isManualProcessing.value
        set(value) {
            _isManualProcessing.value = value
            if (!value) {
                process()
            }
        }

    private val _timer = TimerImpl()
    val timer: Timer = _timer
    private val _executors = AtomicReference<List<Executor>>(emptyList())
    val executors: List<Executor> get() = _executors.value
    private val tasks = AtomicReference<List<Task>>(emptyList())
    private var isProcessing = AtomicBoolean()

    override fun newExecutor(): Executor {
        val executor = ExecutorImpl()
        _executors.change { it + executor }

        return executor
    }

    override fun destroy() {
        _executors
            .getAndChange { emptyList() }
            .forEach(Executor::dispose)
    }

    fun process() {
        if (isProcessing.compareAndSet(false, true)) {
            try {
                processActual()
            } finally {
                isProcessing.value = false
            }
        }
    }

    private fun processActual() {
        while (true) {
            val task = tasks.value.firstOrNull()?.takeIf { it.startTime <= _timer.targetTime } ?: break
            updateTasks {
                removeAt(0)
                if (!task.period.isNegative()) {
                    add(task.copy(startTime = task.startTime + task.period))
                    sort()
                }
            }

            _timer.time = task.startTime

            if (!task.executor.isDisposed) {
                task.task()
            }
        }

        _timer.time = _timer.targetTime
    }

    private fun processIfNeeded() {
        if (!_isManualProcessing.value) {
            process()
        }
    }

    private inline fun updateTasks(block: MutableList<Task>.() -> Unit) {
        tasks.change {
            it.toMutableList().also(block)
        }
    }

    interface Timer {
        val time: Duration

        fun advanceBy(duration: Duration)
    }

    private inner class TimerImpl : Timer {
        private val _millis = AtomicLong()

        override var time: Duration
            get() = _millis.value.milliseconds
            set(value) {
                _millis.value = value.inWholeMilliseconds
            }

        private val _requestedMillis = AtomicLong()
        val targetTime: Duration get() = _requestedMillis.value.milliseconds

        override fun advanceBy(duration: Duration) {
            require(!duration.isNegative()) { "Millis must not be negative" }

            _requestedMillis.addAndGet(duration.inWholeMilliseconds)
            processIfNeeded()
        }
    }

    private inner class ExecutorImpl : Executor {
        private val _isDisposed = AtomicBoolean()
        override val isDisposed: Boolean get() = _isDisposed.value

        override fun submit(startDelay: Duration, period: Duration, task: () -> Unit) {
            addTask(startDelay = startDelay, period = period, task = task)
            processIfNeeded()
        }

        private fun addTask(startDelay: Duration, period: Duration, task: () -> Unit) {
            updateTasks {
                add(
                    Task(
                        startTime = timer.time + startDelay,
                        period = period,
                        executor = this@ExecutorImpl,
                        task = task
                    )
                )
                sort()
            }
        }

        override fun cancel() {
            updateTasks {
                removeAll { it.executor === this@ExecutorImpl }
            }
        }

        override fun dispose() {
            _isDisposed.value = true
            cancel()
        }
    }

    private data class Task(
        val startTime: Duration,
        val period: Duration,
        val executor: Executor,
        val task: () -> Unit
    ) : Comparable<Task> {
        private val sequenceNumber = sequencer.addAndGet(1L)

        override fun compareTo(other: Task): Int =
            if (this === other) {
                0
            } else {
                startTime
                    .compareTo(other.startTime)
                    .takeUnless { it == 0 }
                    ?: sequenceNumber.compareTo(other.sequenceNumber)
            }

        private companion object {
            private val sequencer = AtomicLong()
        }
    }
}
