package com.badoo.gtk3.pointer

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.invoke
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import libgtk3.g_object_unref
import libgtk3.g_signal_connect_data
import libgtk3.gpointer

fun <T : CPointed> UiPointer<*>.ptr(): CPointer<T> = ptr.reinterpret()

fun <S : gpointer> UiPointer<*>.connectSignal(signalName: String, data: S, handler: CPointer<CFunction<(gpointer, S) -> Unit>>) {
    g_signal_connect_data(
        instance = ptr,
        detailed_signal = signalName,
        c_handler = handler.reinterpret(),
        data = data,
        destroy_data = null,
        connect_flags = 0U
    )
}

fun UiPointer<*>.connectSignal(signalName: String, handler: CPointer<CFunction<(gpointer) -> Unit>>) {
    connectSignal(signalName, handler, staticCFunction(::onSignal).reinterpret())
}

private fun onSignal(instance: gpointer, handler: CPointer<CFunction<(gpointer) -> Unit>>) {
    handler.invoke(instance)
}

fun UiPointer<*>.unref() {
    g_object_unref(ptr)
}
