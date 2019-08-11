package com.badoo.reaktive.sample.linux

import com.badoo.gtk3.box.UiBox
import com.badoo.gtk3.box.setChildPackaging
import com.badoo.gtk3.box.uiBox
import com.badoo.gtk3.button.uiButton
import com.badoo.gtk3.container.UiContainer
import com.badoo.gtk3.container.plusAssign
import com.badoo.gtk3.image.setImage
import com.badoo.gtk3.image.uiImage
import com.badoo.gtk3.pointer.connectSignal
import com.badoo.gtk3.spinner.setStarted
import com.badoo.gtk3.spinner.uiSpinner
import com.badoo.gtk3.widget.requestSize
import com.badoo.reaktive.samplemppmodule.view.AbstractKittenView
import com.badoo.reaktive.samplemppmodule.view.KittenView.Event
import com.badoo.reaktive.samplemppmodule.view.KittenView.ViewModel
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.scheduler.mainScheduler
import com.badoo.reaktive.single.observeOn
import com.badoo.reaktive.single.singleFromFunction
import com.badoo.reaktive.single.subscribe
import com.badoo.reaktive.single.subscribeOn
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import sample.libcurl.curl

class KittenViewImpl(container: UiContainer) : AbstractKittenView() {

    private val verticalBox = uiBox(orientation = UiBox.Orientation.VERTICAL, spacing = WIDGET_MARGIN)
    private val horizontalBox = uiBox(orientation = UiBox.Orientation.HORIZONTAL, spacing = WIDGET_MARGIN)
    private val button = uiButton(label = "Load kitten")
    private val spinner = uiSpinner()
    private val image = uiImage()
    private val viewRef = StableRef.create(this)

    init {
        container += verticalBox
        verticalBox += horizontalBox
        verticalBox.setChildPackaging(child = horizontalBox, expand = false, fill = true)
        horizontalBox += button
        horizontalBox.setChildPackaging(child = button, expand = true, fill = true)
        spinner.requestSize(SPINNER_WIDTH, 0)
        horizontalBox += spinner
        horizontalBox.setChildPackaging(spinner, fill = true)
        verticalBox += image
        verticalBox.setChildPackaging(child = image, expand = true, fill = true)

        button.connectSignal(
            "clicked",
            viewRef.asCPointer(),
            staticCFunction { _, v ->
                v.asStableRef<KittenViewImpl>().get().dispatch(Event.Reload)
            }
        )
    }

    fun destroy() {
        viewRef.dispose()
    }

    override fun show(model: ViewModel) {
        loadImage(model.kittenUrl)
        spinner.setStarted(model.isLoading)
    }

    private fun loadImage(url: String?) {
        singleFromFunction { url?.let(::curl) }
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribe(onSuccess = image::setImage)
    }

    private companion object {
        private const val SPINNER_WIDTH = 32
        private const val WIDGET_MARGIN = 8
    }
}