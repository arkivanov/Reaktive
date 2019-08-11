package com.badoo.gtk3.container

import com.badoo.gtk3.pointer.ptr
import com.badoo.gtk3.widget.UiWidget
import libgtk3.gtk_container_add
import libgtk3.gtk_container_get_border_width
import libgtk3.gtk_container_set_border_width

fun <T : UiWidget> UiContainer.addChild(widget: T): T {
    gtk_container_add(ptr(), widget.ptr)

    return widget
}

operator fun UiContainer.plusAssign(widget: UiWidget) {
    addChild(widget)
}

var UiContainer.borderWidth: Int
    get() = gtk_container_get_border_width(ptr()).toInt()
    set(value) {
        gtk_container_set_border_width(ptr(), value.toUInt())
    }
