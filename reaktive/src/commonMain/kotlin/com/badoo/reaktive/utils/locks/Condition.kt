package com.badoo.reaktive.utils.locks

internal expect class Condition

internal expect fun Condition.signal()

internal expect fun Condition.await()

internal expect fun Condition.destroy()
