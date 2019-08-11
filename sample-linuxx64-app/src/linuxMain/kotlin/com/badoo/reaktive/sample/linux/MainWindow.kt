package com.badoo.reaktive.sample.linux

import com.badoo.gtk3.application.UiApplication
import com.badoo.gtk3.applicationwindow.uiApplicationWindow
import com.badoo.gtk3.container.borderWidth
import com.badoo.gtk3.widget.showAll
import com.badoo.gtk3.window.isResizable
import com.badoo.gtk3.window.setDefaultSize
import com.badoo.gtk3.window.title
import com.badoo.reaktive.samplemppmodule.KittenStoreBuilderImpl
import com.badoo.reaktive.samplemppmodule.binder.KittenBinder

class MainWindow(app: UiApplication) {

    private val window = uiApplicationWindow(app)
    private val view: KittenViewImpl = KittenViewImpl(window)
    private val binder = KittenBinder(KittenStoreBuilderImpl())

    init {
        window.title = "Kittens"
        window.setDefaultSize(WINDOW_WIDTH, WINDOW_HEIGHT)
        window.isResizable = false
        window.borderWidth = WINDOW_BORDER_WIDTH

        binder.onViewCreated(view)
        window.showAll()
        binder.onStart()
    }

    fun destroy() {
        view.destroy()
        binder.onStop()
        binder.onViewDestroyed()
        binder.onDestroy()
    }

    private companion object {
        private const val WINDOW_WIDTH = 400
        private const val WINDOW_HEIGHT = 300
        private const val WINDOW_BORDER_WIDTH = 8
    }
}