package com.badoo.gtk3.button

import com.badoo.gtk3.pointer.ptr
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.toKString
import libgtk3.GtkWidget
import libgtk3.gtk_button_get_label
import libgtk3.gtk_button_new_with_label
import libgtk3.gtk_button_set_label

fun uiButton(label: String? = null): UiButton =
    object : UiButton {
        override val ptr: CPointer<GtkWidget> = gtk_button_new_with_label(label)!!
    }

var UiButton.label: String?
    get() = gtk_button_get_label(ptr())?.toKString()
    set(value) {
        gtk_button_set_label(ptr(), value)
    }
