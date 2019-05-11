package com.badoo.reaktive.utils

inline fun <T> SimpleCondition.use(block: (SimpleCondition) -> T): T =
    try {
        block(this)
    } finally {
        destroy()
    }