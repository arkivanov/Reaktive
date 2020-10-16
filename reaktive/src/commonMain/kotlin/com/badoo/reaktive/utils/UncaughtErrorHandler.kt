package com.badoo.reaktive.utils

import com.badoo.reaktive.utils.atomics.AtomicReference
import com.badoo.reaktive.utils.atomics.atomic
import com.badoo.reaktive.utils.atomics.value
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
@Suppress("ObjectPropertyName")
private val _reaktiveUncaughtErrorHandler: AtomicReference<(Throwable) -> Unit> =
    atomic(createDefaultUncaughtErrorHandler())

var reaktiveUncaughtErrorHandler: (Throwable) -> Unit
    get() = _reaktiveUncaughtErrorHandler.value
    set(value) {
        _reaktiveUncaughtErrorHandler.value = value
    }

fun resetReaktiveUncaughtErrorHandler() {
    reaktiveUncaughtErrorHandler = createDefaultUncaughtErrorHandler()
}

internal expect fun createDefaultUncaughtErrorHandler(): (Throwable) -> Unit
