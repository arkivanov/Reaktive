package com.badoo.reaktive.utils

internal actual fun printError(error: Any?) {
    printErrorExternal(error?.toString())
}

@JsFun("(error) => console.error(error)")
internal external fun printErrorExternal(error: String?)
