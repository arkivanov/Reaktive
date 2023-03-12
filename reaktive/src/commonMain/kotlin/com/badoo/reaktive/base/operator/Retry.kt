package com.badoo.reaktive.base.operator

import com.badoo.reaktive.base.ErrorCallback
import com.badoo.reaktive.base.exceptions.CompositeException
import com.badoo.reaktive.base.tryCatch

internal class Retry(
    private val emitter: ErrorCallback,
    private val predicate: (attempt: Long, Throwable) -> Boolean
) {
    private var attempt = 0L

    fun onError(error: Throwable, resubscribe: () -> Unit) {
        emitter.tryCatch(
            { predicate(++attempt, error) },
            { CompositeException(error, it) }
        ) { shouldRetry ->
            if (shouldRetry) {
                resubscribe()
            } else {
                emitter.onError(error)
            }
        }
    }
}
