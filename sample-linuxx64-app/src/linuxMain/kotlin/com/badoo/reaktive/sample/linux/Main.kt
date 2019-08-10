package com.badoo.reaktive.sample.linux

import kotlinx.cinterop.reinterpret
import libgtk3.G_APPLICATION_FLAGS_NONE
import libgtk3.g_application_run
import libgtk3.g_object_unref
import libgtk3.gtk_application_new

private const val WINDOW_WIDTH = 400
private const val WINDOW_HEIGHT = 300
private const val WINDOW_BORDER_WIDTH = 8U

/**
 * How to run:
 * * Install libcurl4-openssl-dev and libgtk-3-dev
 * * Execute ":sample-linuxx64-app:runDebugExecutableLinux" Gradle task
 */
fun main() {
    val app = gtk_application_new("com.badoo.reaktive.sample.linux", G_APPLICATION_FLAGS_NONE)!!

//    val binder = KittenBinder(KittenStoreBuilderImpl())
//    var view: KittenViewImpl? = null
//    var window: CPointer<GtkWindow>? = null

    val activateHandlerId =
        app.connectSignal("activate") {
            //        val w: CPointer<GtkWindow> = gtk_application_window_new(app)!!.reinterpret()
//        window = w
//        gtk_window_set_title(w, "Kittens")
//        gtk_window_set_default_size(w, WINDOW_WIDTH, WINDOW_HEIGHT)
//        gtk_window_set_resizable(w, 0)
//        gtk_container_set_border_width(w.reinterpret(), WINDOW_BORDER_WIDTH)

//        view = KittenViewImpl(w.reinterpret())
//        binder.onViewCreated(view!!)
//        gtk_widget_show_all(w.reinterpret())
//        binder.onStart()
        }

//    app.connectSignal("shutdown") {
//        view!!.destroy()
//        window!!.unref()
//        binder.onStop()
//        binder.onViewDestroyed()
//        binder.onDestroy()
//    }

    g_application_run(app.reinterpret(), 0, null)
    app.disconnectSignal(activateHandlerId)
    g_object_unref(app)
}