package com.badoo

import com.badoo.reaktive.samplemppmodule.view.AbstractKittenView
import com.badoo.reaktive.samplemppmodule.view.KittenView
import org.w3c.dom.Image
import kotlin.browser.document

class KittenViewImpl : AbstractKittenView() {

    private val loader = document.getElementById("loader")!!
    private val kitten = document.getElementById("kitten") as Image

    init {
        document
            .getElementById("load-button")!!
            .addEventListener("click", {
                dispatch(KittenView.Event.Reload)
            })
    }

    override fun show(model: KittenView.ViewModel) {
//        model.error?.handle()?.also {
//            Toast.makeText(root.context, "Error", Toast.LENGTH_LONG).show()
//        }

        if (model.kittenUrl == null) {
            kitten.setAttribute("hidden", "true")
            kitten.src = ""
        } else {
            kitten.removeAttribute("hidden")
            kitten.src = model.kittenUrl!!
        }

        if (model.isLoading) {
            loader.removeAttribute("hidden")
        } else {
            loader.setAttribute("hidden", "true")
        }
    }
}