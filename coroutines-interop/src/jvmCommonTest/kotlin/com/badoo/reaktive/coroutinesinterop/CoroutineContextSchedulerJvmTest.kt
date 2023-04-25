package com.badoo.reaktive.coroutinesinterop

import com.badoo.reaktive.utils.clock.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineContextSchedulerJvmTest {

    private val dispatcher = TestCoroutineDispatcher()
    private val clock = TestClock()
    private val scheduler = CoroutineContextScheduler(context = dispatcher, clock = TestClock())
    private val executor = scheduler.newExecutor()
    private val task = TestTask()

    @Test
    fun executes_task_immediately_WHEN_no_delay() {
        executor.submit(task = task::run)

        task.assertSingleRun()
    }

    @Test
    fun does_not_execute_task_WHEN_delay_not_reached() {
        executor.submit(delay = 100.milliseconds, task = task::run)
        advanceTimeBy(99.milliseconds)

        task.assertDidNotRun()
    }

    @Test
    fun executes_task_WHEN_delay_reached() {
        executor.submit(delay = 100.milliseconds, task = task::run)
        advanceTimeBy(100.milliseconds)

        task.assertSingleRun()
    }

    @Test
    fun executes_repeating_task_immediately_WHEN_no_delay() {
        executor.submit(period = 1.seconds, task = task::run)

        task.assertSingleRun()
    }

    @Test
    fun does_not_execute_repeating_task_WHEN_delay_not_reached() {
        executor.submit(delay = 100.milliseconds, period = 1.seconds, task = task::run)
        advanceTimeBy(99.milliseconds)

        task.assertDidNotRun()
    }

    @Test
    fun executes_repeating_task_WHEN_delay_reached() {
        executor.submit(delay = 100.milliseconds, period = 1.seconds, task = task::run)
        advanceTimeBy(100.milliseconds)

        task.assertSingleRun()
    }

    @Test
    fun does_not_execute_repeating_task_second_time_WHEN_period_not_reached() {
        executor.submit(delay = 100.milliseconds, period = 1.seconds, task = task::run)
        advanceTimeBy(100.milliseconds)
        task.reset()
        advanceTimeBy(999.milliseconds)

        task.assertDidNotRun()
    }

    @Test
    fun executes_repeating_task_second_time_WHEN_period_reached() {
        executor.submit(delay = 100.milliseconds, period = 1.seconds, task = task::run)
        advanceTimeBy(100.milliseconds)
        task.reset()
        advanceTimeBy(1.seconds)

        task.assertSingleRun()
    }

    @Test
    fun does_not_execute_repeating_task_third_time_WHEN_period_not_reached_second_time() {
        executor.submit(delay = 100.milliseconds, period = 1.seconds, task = task::run)
        advanceTimeBy(100.milliseconds)
        advanceTimeBy(1.seconds)
        task.reset()
        advanceTimeBy(999.milliseconds)

        task.assertDidNotRun()
    }

    @Test
    fun executes_repeating_task_third_time_WHEN_period_reached_second_time() {
        executor.submit(delay = 100.milliseconds, period = 1.seconds, task = task::run)
        advanceTimeBy(100.milliseconds)
        advanceTimeBy(1.seconds)
        task.reset()
        advanceTimeBy(1.seconds)

        task.assertSingleRun()
    }

    @Test
    fun does_not_execute_task_WHEN_executor_cancelled_and_delay_reached() {
        executor.submit(delay = 100.milliseconds, task = task::run)
        executor.cancel()
        advanceTimeBy(100.milliseconds)

        task.assertDidNotRun()
    }

    @Test
    fun does_not_execute_repeating_task_WHEN_executor_cancelled_and_delay_reached() {
        executor.submit(delay = 100.milliseconds, period = 1.seconds, task = task::run)
        executor.cancel()
        advanceTimeBy(100.milliseconds)

        task.assertDidNotRun()
    }

    @Test
    fun does_not_execute_repeating_task_second_time_WHEN_executor_cancelled_and_period_reached() {
        executor.submit(delay = 100.milliseconds, period = 1.seconds, task = task::run)
        advanceTimeBy(100.milliseconds)
        task.reset()
        executor.cancel()
        advanceTimeBy(1.seconds)

        task.assertDidNotRun()
    }

    @Test
    fun does_not_execute_repeating_task_third_time_WHEN_executor_cancelled_and_period_reached_second_time() {
        executor.submit(delay = 100.milliseconds, period = 1.seconds, task = task::run)
        advanceTimeBy(100.milliseconds)
        advanceTimeBy(1.seconds)
        task.reset()
        executor.cancel()
        advanceTimeBy(1.seconds)

        task.assertDidNotRun()
    }

    @Test
    fun does_not_execute_task_WHEN_executor_disposed_and_delay_reached() {
        executor.submit(delay = 100.milliseconds, task = task::run)
        executor.dispose()
        advanceTimeBy(100.milliseconds)

        task.assertDidNotRun()
    }

    @Test
    fun does_not_execute_repeating_task_WHEN_executor_disposed_and_delay_reached() {
        executor.submit(delay = 100.milliseconds, period = 1.seconds, task = task::run)
        executor.dispose()
        advanceTimeBy(100.milliseconds)

        task.assertDidNotRun()
    }

    @Test
    fun does_not_execute_repeating_task_second_time_WHEN_executor_disposed_and_period_reached() {
        executor.submit(delay = 100.milliseconds, period = 1.seconds, task = task::run)
        advanceTimeBy(100.milliseconds)
        task.reset()
        executor.dispose()
        advanceTimeBy(1.seconds)

        task.assertDidNotRun()
    }

    @Test
    fun does_not_execute_repeating_task_third_time_WHEN_executor_disposed_and_period_reached_second_time() {
        executor.submit(delay = 100.milliseconds, period = 1.seconds, task = task::run)
        advanceTimeBy(100.milliseconds)
        advanceTimeBy(1.seconds)
        task.reset()
        executor.dispose()
        advanceTimeBy(1.seconds)

        task.assertDidNotRun()
    }

    @Test
    fun does_not_execute_task_WHEN_scheduler_destroyed_and_delay_reached() {
        executor.submit(delay = 100.milliseconds, task = task::run)
        scheduler.destroy()
        advanceTimeBy(100.milliseconds)

        task.assertDidNotRun()
    }

    @Test
    fun does_not_execute_repeating_task_WHEN_scheduler_destroyed_and_delay_reached() {
        executor.submit(delay = 100.milliseconds, period = 1.seconds, task = task::run)
        scheduler.destroy()
        advanceTimeBy(100.milliseconds)

        task.assertDidNotRun()
    }

    @Test
    fun does_not_execute_repeating_task_second_time_WHEN_scheduler_destroyed_and_period_reached() {
        executor.submit(delay = 100.milliseconds, period = 1.seconds, task = task::run)
        advanceTimeBy(100.milliseconds)
        task.reset()
        scheduler.destroy()
        advanceTimeBy(1.seconds)

        task.assertDidNotRun()
    }

    @Test
    fun does_not_execute_repeating_task_third_time_WHEN_scheduler_destroyed_and_period_reached_second_time() {
        executor.submit(delay = 100.milliseconds, period = 1.seconds, task = task::run)
        advanceTimeBy(100.milliseconds)
        advanceTimeBy(1.seconds)
        task.reset()
        scheduler.destroy()
        advanceTimeBy(1.seconds)

        task.assertDidNotRun()
    }

    private fun advanceTimeBy(duration: Duration) {
        clock.advanceBy(duration)

        dispatcher.scheduler.apply {
            advanceTimeBy(duration.inWholeMilliseconds)
            runCurrent()
        }
    }

    private class TestClock : Clock {
        override var uptime: Duration = Duration.ZERO

        fun advanceBy(duration: Duration) {
            uptime += duration
        }
    }

    private class TestTask {
        private var runCount = 0

        fun run() {
            runCount++
        }

        fun assertDidNotRun() {
            assertEquals(0, runCount)
        }

        fun assertSingleRun() {
            assertEquals(1, runCount)
        }

        fun reset() {
            runCount = 0
        }
    }
}
