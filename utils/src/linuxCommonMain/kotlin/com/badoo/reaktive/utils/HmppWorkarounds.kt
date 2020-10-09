package com.badoo.reaktive.utils

import platform.posix.pthread_t
import platform.posix.timespec

internal expect operator fun timespec.plusAssign(nanos: Long)

expect fun pthread_t.toLong(): Long
