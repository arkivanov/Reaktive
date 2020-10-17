package com.badoo.reaktive.utils.locks

@Suppress("ACTUAL_WITHOUT_EXPECT") // Workaround https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias Condition = java.util.concurrent.locks.Condition

internal actual inline fun Condition.signal() {
    signalAll()
}

internal actual inline fun Condition.await() {
    await()
}

internal actual inline fun Condition.destroy() {
}
