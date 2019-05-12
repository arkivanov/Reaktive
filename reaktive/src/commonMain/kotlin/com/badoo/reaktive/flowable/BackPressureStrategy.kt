package com.badoo.reaktive.flowable

class BackPressureStrategy(
    val bufferSize: Int = Int.MAX_VALUE,
    val overflowStrategy: OverflowStrategy = OverflowStrategy.DROP_OLDEST
) {

    enum class OverflowStrategy {
        DROP_OLDEST, DROP_LATEST, ERROR
    }

    companion object {
        val DEFAULT = BackPressureStrategy()
    }
}