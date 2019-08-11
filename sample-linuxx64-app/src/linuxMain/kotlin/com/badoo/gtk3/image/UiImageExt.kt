package com.badoo.gtk3.image

import com.badoo.gtk3.pointer.ptr
import com.badoo.gtk3.widget.allocatedHeight
import com.badoo.gtk3.widget.allocatedWidth
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.value
import libgtk3.GBytes
import libgtk3.GInputStream
import libgtk3.GdkPixbuf
import libgtk3.GtkWidget
import libgtk3.g_bytes_new
import libgtk3.g_memory_input_stream_new_from_bytes
import libgtk3.gdk_pixbuf_new_from_stream_at_scale
import libgtk3.gtk_image_clear
import libgtk3.gtk_image_new
import libgtk3.gtk_image_set_from_pixbuf


fun uiImage(): UiImage =
    object : UiImage {
        override val ptr: CPointer<GtkWidget> = gtk_image_new()!!
    }

fun UiImage.setImage(data: ByteArray?) {
    if ((data == null) || data.isEmpty()) {
        clearImage()

        return
    }

    memScoped {
        val arr: CArrayPointer<ByteVar> =
            allocArray(data.size) { index: Int ->
                value = data[index]
            }

        val b: CPointer<GBytes> = g_bytes_new(arr, data.size.convert())!!
        val s: CPointer<GInputStream> = g_memory_input_stream_new_from_bytes(b)!!

        val p: CPointer<GdkPixbuf> =
            gdk_pixbuf_new_from_stream_at_scale(
                stream = s,
                width = allocatedWidth,
                height = allocatedHeight,
                preserve_aspect_ratio = 1,
                cancellable = null,
                error = null
            )!!

        gtk_image_set_from_pixbuf(ptr(), p)
    }
}

fun UiImage.clearImage() {
    gtk_image_clear(ptr())
}
