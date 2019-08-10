package com.badoo.reaktive.sample.linux

import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import libgtk3.g_object_unref
import libgtk3.g_signal_connect_data
import libgtk3.g_signal_handler_disconnect
import libgtk3.gpointer
import libgtk3.gulong

fun gpointer.connectSignal(signalName: String, handler: () -> Unit): gulong =
    g_signal_connect_data(
        instance = this,
        detailed_signal = signalName,
        c_handler = staticCFunction<gpointer, gpointer, Unit> { _, data ->
            @Suppress("UNCHECKED_CAST")
            data.asStableRef<() -> Unit>().get().invoke()
        }.reinterpret(),
        data = StableRef.create(handler).asCPointer(),
        destroy_data = null,
        connect_flags = 0U
    )

fun gpointer.disconnectSignal(handlerId: gulong) {
    g_signal_handler_disconnect(this, handlerId)
}

fun gpointer.unref() {
    g_object_unref(this)
}