package com.badoo.reaktive.scheduler

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.minusAssign
import com.badoo.reaktive.disposable.plusAssign
import com.badoo.reaktive.global.external.globalThis
import kotlin.time.Duration

internal class MainScheduler : Scheduler {

    private val disposables = CompositeDisposable()

    override fun newExecutor(): Scheduler.Executor = MainThreadExecutor(disposables)

    override fun destroy() = disposables.dispose()

    private class MainThreadExecutor(
        private val disposables: CompositeDisposable
    ) : Scheduler.Executor {

        private var _isDisposed = false

        private val timeoutIds = mutableSetOf<dynamic>()
        private val intervalIds = mutableSetOf<dynamic>()

        init {
            disposables += this
        }

        override fun submit(startDelay: Duration, period: Duration, task: () -> Unit) {
            if (period.isInfinite()) {
                setTimeout(delay = startDelay, task = task)
                return
            }

            if (startDelay.isPositive()) {
                setTimeout(delay = startDelay) {
                    setInterval(period = period, task)
                }
            } else {
                setInterval(period = period, task)
            }
        }

        private fun setTimeout(delay: Duration, task: () -> Unit) {
            var id: dynamic = undefined

            id =
                globalThis.setInterval(
                    {
                        intervalIds.remove(id)
                        task()
                    },
                    delay.inWholeMilliseconds.toInt(),
                )

            intervalIds.add(id)
        }

        private fun setInterval(period: Duration, task: () -> Unit) {
            var id: dynamic = undefined

            id =
                globalThis.setInterval(
                    {
                        intervalIds.remove(id)
                        task()
                    },
                    period.inWholeMilliseconds.toInt(),
                )

            intervalIds.add(id)
        }

        override fun cancel() {
            timeoutIds.forEach { globalThis.clearTimeout(it) }
            intervalIds.forEach { globalThis.clearInterval(it) }
        }

        override val isDisposed: Boolean
            get() = _isDisposed

        override fun dispose() {
            cancel()
            _isDisposed = true
            disposables -= this
        }
    }
}
