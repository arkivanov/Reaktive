package com.badoo.gtk3.applicationwindow

import com.badoo.gtk3.application.UiApplication
import com.badoo.gtk3.container.addChild
import com.badoo.gtk3.widget.UiWidget
import kotlinx.cinterop.CPointer
import libgtk3.GtkWidget
import libgtk3.gtk_application_window_new

fun uiApplicationWindow(application: UiApplication): UiApplicationWindow =
    object : UiApplicationWindow {
        override fun <T : UiWidget> T.unaryPlus(): T = addChild(this)

        override val ptr: CPointer<GtkWidget> = gtk_application_window_new(application.ptr)!!
    }
