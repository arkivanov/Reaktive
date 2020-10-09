package com.badoo.reaktive.utils

import platform.posix.pthread_t
import platform.posix.timespec

internal actual operator fun timespec.plusAssign(nanos: Long) {
    tv_sec += (nanos / NANOS_IN_SECOND)
    tv_nsec += (nanos % NANOS_IN_SECOND)
    if (tv_nsec >= NANOS_IN_SECOND) {
        tv_sec += 1
        tv_nsec -= NANOS_IN_SECOND
    }
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun pthread_t.toLong(): Long = this.toLong()
