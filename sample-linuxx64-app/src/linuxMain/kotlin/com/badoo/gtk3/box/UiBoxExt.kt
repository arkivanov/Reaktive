package com.badoo.gtk3.box

import com.badoo.gtk3.box.UiBox.Orientation
import com.badoo.gtk3.container.addChild
import com.badoo.gtk3.pointer.ptr
import com.badoo.gtk3.widget.UiWidget
import com.badoo.reaktive.sample.linux.gbooleanValue
import kotlinx.cinterop.CPointer
import libgtk3.GtkOrientation
import libgtk3.GtkPackType
import libgtk3.GtkWidget
import libgtk3.gtk_box_get_spacing
import libgtk3.gtk_box_new
import libgtk3.gtk_box_set_child_packing
import libgtk3.gtk_box_set_spacing

inline fun uiBox(
    orientation: Orientation = Orientation.VERTICAL,
    spacing: Int = 0,
    initialize: UiBox.() -> Unit = {}
): UiBox =
    object : UiBox {
        override val ptr: CPointer<GtkWidget> = gtk_box_new(orientation.gtkOrientation, spacing)!!

        override fun <T : UiWidget> T.unaryPlus(): T = addChild(this)

        private val Orientation.gtkOrientation: GtkOrientation
            get() =
                when (this) {
                    Orientation.HORIZONTAL -> GtkOrientation.GTK_ORIENTATION_HORIZONTAL
                    Orientation.VERTICAL -> GtkOrientation.GTK_ORIENTATION_VERTICAL
                }
    }
        .also(initialize)

var UiBox.spacing: Int
    get() = gtk_box_get_spacing(ptr())
    set(value) {
        gtk_box_set_spacing(ptr(), value)
    }

fun UiBox.setChildPackaging(child: UiWidget, expand: Boolean = false, fill: Boolean = false, padding: Int = 0) {
    gtk_box_set_child_packing(
        box = ptr(),
        child = child.ptr(),
        expand = expand.gbooleanValue,
        fill = fill.gbooleanValue,
        padding = padding.toUInt(),
        pack_type = GtkPackType.GTK_PACK_START
    )
}
