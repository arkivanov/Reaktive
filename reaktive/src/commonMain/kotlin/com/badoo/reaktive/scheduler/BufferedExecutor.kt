package com.badoo.reaktive.scheduler

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.BufferOverflowException
import com.badoo.reaktive.observable.BufferOverflowStrategy
import com.badoo.reaktive.utils.RefCounter
import com.badoo.reaktive.utils.atomic.AtomicBoolean
import com.badoo.reaktive.utils.lock.Lock
import com.badoo.reaktive.utils.lock.synchronized
import com.badoo.reaktive.utils.queue.SharedQueue
import com.badoo.reaktive.utils.use

internal class BufferedExecutor<in T>(
    private val executor: Scheduler.Executor,
    private val bufferSize: Int,
    private val bufferOverflowStrategy: BufferOverflowStrategy,
    private val onNext: (T) -> Unit
) : Disposable {

    private val queue = SharedQueue<T>()
    private val isDraining = AtomicBoolean()
    private val drainFunction = ::drain

    private val _isDisposed = AtomicBoolean()
    override val isDisposed: Boolean get() = _isDisposed.value

    private val lock = Lock()
    private val condition = lock.newCondition()
    private val refCounter = RefCounter(::destroy)

    private fun destroy() {
        condition.destroy()
        lock.destroy()
    }

    override fun dispose() {
        synchronized {
            _isDisposed.value = true
            refCounter.release()
            condition.signal()
        }
    }

    fun submit(value: T) {
        synchronized {
            if (_isDisposed.value) {
                return
            }

            if (queue.size < bufferSize) {
                queue.offer(value)
            } else {
                when (bufferOverflowStrategy) {
                    BufferOverflowStrategy.ERROR -> throw BufferOverflowException()

                    BufferOverflowStrategy.DROP_OLDEST -> {
                        queue.poll()
                        queue.offer(value)
                    }

                    BufferOverflowStrategy.DROP_NEWEST -> Unit // no-op

                    BufferOverflowStrategy.BLOCK -> {
                        while (queue.size >= bufferSize) {
                            if (_isDisposed.value) {
                                return
                            }
                            condition.await()
                        }
                        queue.offer(value)
                    }
                }.let {}
            }

            if (isDraining.compareAndSet(false, true)) {
                executor.submit(delayMillis = 0L, task = drainFunction)
            }
        }
    }

    private fun drain() {
        while (!isDisposed) {
            synchronized(onResult = onNext) {
                if (queue.isEmpty) {
                    isDraining.value = false
                    return
                }

                @Suppress("UNCHECKED_CAST")
                val item = queue.poll() as T
                if (bufferOverflowStrategy == BufferOverflowStrategy.BLOCK) {
                    condition.signal()
                }
                item
            }
        }
    }

    private inline fun <T> synchronized(onResult: (T) -> Unit = {}, block: () -> T) {
        refCounter.use {
            onResult(lock.synchronized(block))
        }
    }
}
