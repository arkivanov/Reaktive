package com.badoo.reaktive.sample.linux

import com.badoo.gtk3.application.run
import com.badoo.gtk3.application.uiApplication
import com.badoo.gtk3.pointer.connectSignal
import com.badoo.gtk3.pointer.unref
import kotlinx.cinterop.staticCFunction
import libgtk3.gpointer

private val application = uiApplication("com.badoo.reaktive.sample.linux")
private lateinit var mainWindow: MainWindow

/**
 * How to run:
 * * Install libcurl4-openssl-dev and libgtk-3-dev
 * * Execute ":sample-linuxx64-app:runDebugExecutableLinux" Gradle task
 */
fun main() {
    application.connectSignal("activate", staticCFunction(::activate))
    application.connectSignal("shutdown", staticCFunction(::shutdown))
    application.run()
    application.unref()
}

private fun activate(@Suppress("UNUSED_PARAMETER") app: gpointer) {
    mainWindow = MainWindow(application)
}

private fun shutdown(@Suppress("UNUSED_PARAMETER") app: gpointer) {
    mainWindow.destroy()
}