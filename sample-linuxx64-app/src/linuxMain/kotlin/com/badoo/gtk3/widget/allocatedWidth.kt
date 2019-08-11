package com.badoo.gtk3.widget

import com.badoo.gtk3.pointer.ptr
import libgtk3.gtk_widget_get_allocated_height
import libgtk3.gtk_widget_get_allocated_width
import libgtk3.gtk_widget_set_size_request
import libgtk3.gtk_widget_show_all

val UiWidget.allocatedWidth: Int get() = gtk_widget_get_allocated_width(ptr)

val UiWidget.allocatedHeight: Int get() = gtk_widget_get_allocated_height(ptr)

fun UiWidget.requestSize(width: Int, height: Int) {
    gtk_widget_set_size_request(ptr, width, height)
}

fun UiWidget.showAll() {
    gtk_widget_show_all(ptr())
}
