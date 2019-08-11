package com.badoo.gtk3.box

import com.badoo.gtk3.container.UiContainer

interface UiBox : UiContainer {

    enum class Orientation {
        HORIZONTAL, VERTICAL
    }
}
