package com.badoo.reaktive.sample.linux

import com.badoo.reaktive.samplemppmodule.AbstractKittenView
import com.badoo.reaktive.samplemppmodule.KittenView.Event
import com.badoo.reaktive.samplemppmodule.KittenView.ViewModel
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.scheduler.mainScheduler
import com.badoo.reaktive.single.observeOn
import com.badoo.reaktive.single.singleFromFunction
import com.badoo.reaktive.single.subscribe
import com.badoo.reaktive.single.subscribeOn
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import libgtk3.GBytes
import libgtk3.GInputStream
import libgtk3.GdkPixbuf
import libgtk3.GtkContainer
import libgtk3.GtkImage
import libgtk3.GtkOrientation
import libgtk3.GtkPackType
import libgtk3.GtkSpinner
import libgtk3.g_bytes_new
import libgtk3.g_memory_input_stream_new_from_bytes
import libgtk3.gdk_pixbuf_new_from_stream_at_scale
import libgtk3.gtk_box_new
import libgtk3.gtk_box_set_child_packing
import libgtk3.gtk_box_set_spacing
import libgtk3.gtk_button_new_with_label
import libgtk3.gtk_container_add
import libgtk3.gtk_image_clear
import libgtk3.gtk_image_new
import libgtk3.gtk_image_set_from_pixbuf
import libgtk3.gtk_spinner_new
import libgtk3.gtk_spinner_start
import libgtk3.gtk_spinner_stop
import libgtk3.gtk_widget_get_allocated_height
import libgtk3.gtk_widget_get_allocated_width
import libgtk3.gtk_widget_set_margin_end
import libgtk3.gtk_widget_set_margin_start
import libgtk3.gtk_widget_set_margin_top
import libgtk3.gtk_widget_set_size_request
import sample.libcurl.curl

class KittenViewImpl(container: CPointer<GtkContainer>) : AbstractKittenView() {

    private val verticalBox = gtk_box_new(GtkOrientation.GTK_ORIENTATION_VERTICAL, 0)!!
    private val horizontalBox = gtk_box_new(GtkOrientation.GTK_ORIENTATION_HORIZONTAL, 0)!!
    private val button = gtk_button_new_with_label("Load kitten")!!
    private val spinner = gtk_spinner_new()!!.reinterpret<GtkSpinner>()
    private val image = gtk_image_new()!!.reinterpret<GtkImage>()

    init {
        gtk_container_add(container.reinterpret(), verticalBox)
        gtk_box_set_child_packing(verticalBox.reinterpret(), horizontalBox, 1, 1, 0, GtkPackType.GTK_PACK_START)
        gtk_box_set_spacing(verticalBox.reinterpret(), WIDGET_MARGIN)

        gtk_container_add(verticalBox.reinterpret(), horizontalBox)
        gtk_box_set_child_packing(verticalBox.reinterpret(), horizontalBox, 0, 1, 0, GtkPackType.GTK_PACK_START)
        gtk_box_set_spacing(horizontalBox.reinterpret(), WIDGET_MARGIN)

        gtk_container_add(horizontalBox.reinterpret(), button.reinterpret())
        gtk_box_set_child_packing(horizontalBox.reinterpret(), button, 1, 1, 0, GtkPackType.GTK_PACK_START)

        gtk_widget_set_size_request(spinner.reinterpret(), SPINNER_WIDTH, 0)
        gtk_container_add(horizontalBox.reinterpret(), spinner.reinterpret())
        gtk_box_set_child_packing(horizontalBox.reinterpret(), spinner.reinterpret(), 0, 1, 0, GtkPackType.GTK_PACK_START)

        gtk_container_add(verticalBox.reinterpret(), image.reinterpret())
        gtk_box_set_child_packing(verticalBox.reinterpret(), image.reinterpret(), 1, 1, 0, GtkPackType.GTK_PACK_START)

        button.connectSignal("clicked") {
            dispatch(Event.Reload)
        }
    }

    fun destroy() {
//        verticalBox.unref()
//        horizontalBox.unref()
//        button.unref()
//        spinner.unref()
//        image.unref()
    }

    override fun show(model: ViewModel) {
        loadImage(model.kittenUrl)

        if (model.isLoading) {
            gtk_spinner_start(spinner)
        } else {
            gtk_spinner_stop(spinner)
        }
    }

    private fun loadImage(url: String?) {
        singleFromFunction { url?.let(::curl) }
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribe(onSuccess = ::setImage)
    }

    private fun setImage(bytes: ByteArray?) {
        if (bytes == null) {
            gtk_image_clear(image)
        } else {
            memScoped {
                val arr: CArrayPointer<ByteVar> =
                    allocArray(bytes.size) { index: Int ->
                        value = bytes[index]
                    }

                val b: CPointer<GBytes> = g_bytes_new(arr, bytes.size.convert())!!
                val s: CPointer<GInputStream> = g_memory_input_stream_new_from_bytes(b)!!
                val width = gtk_widget_get_allocated_width(image.reinterpret())
                val height = gtk_widget_get_allocated_height(image.reinterpret())
                val p: CPointer<GdkPixbuf> = gdk_pixbuf_new_from_stream_at_scale(s, width, height, 1, null, null)!!
                gtk_image_set_from_pixbuf(image, p)
            }
        }
    }

    private companion object {
        private const val SPINNER_WIDTH = 32
        private const val WIDGET_MARGIN = 8
    }
}