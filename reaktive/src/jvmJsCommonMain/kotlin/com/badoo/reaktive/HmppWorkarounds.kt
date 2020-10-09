package com.badoo.reaktive

internal expect inline fun <T> Any.synchronized(block: () -> T): T
