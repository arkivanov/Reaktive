package com.badoo.reaktive.flowable

class BackPressureStrategy(
    val bufferSize: Int = Int.MAX_VALUE,
    val overflowStrategy: OverflowStrategy = OverflowStrategy.ERROR
) {

    enum class OverflowStrategy {
        ERROR, DROP_OLDEST, DROP_LATEST
    }

    companion object {
        val DEFAULT = BackPressureStrategy()
    }
}