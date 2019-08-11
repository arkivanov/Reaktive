package com.badoo.gtk3.pointer

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer

interface UiPointer<T : CPointed> {

    val ptr: CPointer<T>
}