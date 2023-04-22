package com.badoo.reaktive.utils.lock

import com.badoo.reaktive.utils.InternalReaktiveApi
import com.badoo.reaktive.utils.NANOS_IN_MICRO
import com.badoo.reaktive.utils.NANOS_IN_SECOND
import kotlinx.cinterop.Arena
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.ETIMEDOUT
import platform.posix.PTHREAD_MUTEX_RECURSIVE
import platform.posix.__darwin_time_t
import platform.posix.gettimeofday
import platform.posix.pthread_cond_broadcast
import platform.posix.pthread_cond_destroy
import platform.posix.pthread_cond_init
import platform.posix.pthread_cond_t
import platform.posix.pthread_cond_timedwait
import platform.posix.pthread_cond_wait
import platform.posix.pthread_mutex_destroy
import platform.posix.pthread_mutex_init
import platform.posix.pthread_mutex_lock
import platform.posix.pthread_mutex_t
import platform.posix.pthread_mutex_unlock
import platform.posix.pthread_mutexattr_destroy
import platform.posix.pthread_mutexattr_init
import platform.posix.pthread_mutexattr_settype
import platform.posix.pthread_mutexattr_t
import platform.posix.timespec
import platform.posix.timeval
import kotlin.native.internal.createCleaner
import kotlin.system.getTimeNanos

@InternalReaktiveApi
actual open class Lock {

    private val arena = Arena()
    private val attr = arena.alloc<pthread_mutexattr_t>()
    private val mutex = arena.alloc<pthread_mutex_t>()

    @Suppress("unused") // Must be stored in a property
    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = createCleaner(Resources(arena, attr, mutex), Resources::destroy)

    init {
        pthread_mutexattr_init(attr.ptr)
        pthread_mutexattr_settype(attr.ptr, PTHREAD_MUTEX_RECURSIVE)
        pthread_mutex_init(mutex.ptr, attr.ptr)
    }

    @Suppress("MemberNameEqualsClassName") // Matches expect class
    actual fun lock() {
        pthread_mutex_lock(mutex.ptr)
    }

    actual fun unlock() {
        pthread_mutex_unlock(mutex.ptr)
    }

    actual fun newCondition(): Condition = ConditionImpl(mutex.ptr)

    private class Resources(
        val arena: Arena,
        val attr: pthread_mutexattr_t,
        val mutex: pthread_mutex_t,
    ) {
        fun destroy() {
            pthread_mutex_destroy(mutex.ptr)
            pthread_mutexattr_destroy(attr.ptr)
            arena.clear()
        }
    }

    private class ConditionImpl(
        private val lockPtr: CPointer<pthread_mutex_t>
    ) : Condition {

        private val arena = Arena()
        private val cond = arena.alloc<pthread_cond_t>()

        @Suppress("unused") // Must be stored in a property
        @OptIn(ExperimentalStdlibApi::class)
        private val cleaner = createCleaner(Resources(arena, cond), Resources::destroy)

        init {
            pthread_cond_init(cond.ptr, null)
        }

        override fun await() {
            val result = pthread_cond_wait(cond.ptr, lockPtr)
            check(result == 0) { "Error waiting for condition: $result" }
        }

        override fun awaitNanos(nanos: Long): Long =
            memScoped {
                // can't use monotonic time, pthread_condattr_setclock() nor clock_gettime(),
                // iOS does not support it
                // can't use NSRecursiveLock and NSCondition,
                // it can't wait less than 1 second and lock can't create condition
                val tv = alloc<timeval> { gettimeofday(ptr, null) }
                val ts = alloc<timespec> { set(tv) }
                ts += nanos
                val startNanos = getTimeNanos()

                return when (val result = pthread_cond_timedwait(cond.ptr, lockPtr, ts.ptr)) {
                    0 -> startNanos + nanos - getTimeNanos()
                    ETIMEDOUT -> 0L
                    else -> error("Error waiting for condition: $result")
                }
            }

        override fun signalAll() {
            pthread_cond_broadcast(cond.ptr)
        }

        private companion object {
            @OptIn(UnsafeNumber::class)
            private fun timespec.set(time: timeval) {
                tv_sec = time.tv_sec
                tv_nsec = (time.tv_usec * NANOS_IN_MICRO).convert()
            }

            @OptIn(UnsafeNumber::class)
            private operator fun timespec.plusAssign(nanos: Long) {
                tv_sec += (nanos / NANOS_IN_SECOND).convert<__darwin_time_t>()
                tv_nsec += (nanos % NANOS_IN_SECOND).convert<__darwin_time_t>()
                if (tv_nsec >= NANOS_IN_SECOND) {
                    tv_sec += 1
                    tv_nsec -= NANOS_IN_SECOND.convert<__darwin_time_t>()
                }
            }
        }

        private class Resources(
            val arena: Arena,
            val cond: pthread_cond_t,
        ) {
            fun destroy() {
                pthread_cond_destroy(cond.ptr)
                arena.clear()
            }
        }
    }
}
