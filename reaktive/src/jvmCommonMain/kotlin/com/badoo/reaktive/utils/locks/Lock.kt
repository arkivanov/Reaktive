package com.badoo.reaktive.utils.locks

@Suppress("ACTUAL_WITHOUT_EXPECT") // Workaround https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias Lock = java.util.concurrent.locks.ReentrantLock

internal actual inline fun Lock.acquire() {
    lock()
}

internal actual inline fun Lock.release() {
    unlock()
}

internal actual inline fun Lock.destroy() {
}

internal actual inline fun Lock.newCondition(): Condition = newCondition()
