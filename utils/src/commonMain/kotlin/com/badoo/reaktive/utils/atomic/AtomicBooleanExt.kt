package com.badoo.reaktive.utils.atomic

import kotlin.reflect.KProperty

internal operator fun AtomicBoolean.getValue(thisRef: Any?, property: KProperty<*>): Boolean = value

internal operator fun AtomicBoolean.setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
    this.value = value
}
