package com.badoo.reaktive.utils.locks

internal expect class Lock()

internal expect fun Lock.acquire()

internal expect fun Lock.release()

internal expect fun Lock.destroy()

internal expect fun Lock.newCondition(): Condition
