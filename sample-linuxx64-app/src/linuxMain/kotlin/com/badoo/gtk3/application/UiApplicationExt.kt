package com.badoo.gtk3.application

import com.badoo.gtk3.pointer.ptr
import kotlinx.cinterop.CPointer
import libgtk3.G_APPLICATION_FLAGS_NONE
import libgtk3.GtkApplication
import libgtk3.g_application_run
import libgtk3.gtk_application_new

fun uiApplication(id: String): UiApplication =
    object : UiApplication {
        override val ptr: CPointer<GtkApplication> = gtk_application_new(id, G_APPLICATION_FLAGS_NONE)!!
    }

fun UiApplication.run() {
    g_application_run(ptr(), 0, null)
}
