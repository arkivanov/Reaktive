package com.badoo.reaktive.utils

var reaktiveUncaughtErrorHandler: (Throwable) -> Unit =
    createDefaultUncaughtErrorHandler()

fun resetReaktiveUncaughtErrorHandler() {
    reaktiveUncaughtErrorHandler = createDefaultUncaughtErrorHandler()
}

internal expect fun createDefaultUncaughtErrorHandler(): (Throwable) -> Unit
