package com.badoo.reaktive

internal actual inline fun <T> Any.synchronized(block: () -> T): T = synchronized(this, block)
