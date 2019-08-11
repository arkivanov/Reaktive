package com.badoo.gtk3.spinner

import com.badoo.gtk3.pointer.ptr
import kotlinx.cinterop.CPointer
import libgtk3.GtkWidget
import libgtk3.gtk_spinner_new
import libgtk3.gtk_spinner_start
import libgtk3.gtk_spinner_stop

fun uiSpinner(): UiSpinner =
    object : UiSpinner {
        override val ptr: CPointer<GtkWidget> = gtk_spinner_new()!!
    }

fun UiSpinner.start() {
    gtk_spinner_start(ptr())
}

fun UiSpinner.stop() {
    gtk_spinner_stop(ptr())
}

fun UiSpinner.setStarted(isStarted: Boolean) {
    if (isStarted) {
        start()
    } else {
        stop()
    }
}