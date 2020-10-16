package com.badoo.reaktive.utils.atomic

import kotlin.reflect.KProperty

internal operator fun AtomicLong.getValue(thisRef: Any?, property: KProperty<*>): Long = value

internal operator fun AtomicLong.setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
    this.value = value
}
