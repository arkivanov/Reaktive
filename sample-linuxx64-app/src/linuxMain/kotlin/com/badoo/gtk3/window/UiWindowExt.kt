package com.badoo.gtk3.window

import com.badoo.gtk3.pointer.ptr
import com.badoo.reaktive.sample.linux.booleanValue
import com.badoo.reaktive.sample.linux.gbooleanValue
import kotlinx.cinterop.toKString
import libgtk3.gtk_window_get_resizable
import libgtk3.gtk_window_get_title
import libgtk3.gtk_window_set_default_size
import libgtk3.gtk_window_set_resizable
import libgtk3.gtk_window_set_title

var UiWindow.title: String?
    get() = gtk_window_get_title(ptr())?.toKString()
    set(value) {
        gtk_window_set_title(ptr(), value)
    }

var UiWindow.isResizable: Boolean
    get() = gtk_window_get_resizable(ptr()).booleanValue
    set(value) {
        gtk_window_set_resizable(ptr(), value.gbooleanValue)
    }

fun UiWindow.setDefaultSize(width: Int, height: Int) {
    gtk_window_set_default_size(ptr(), width, height)
}