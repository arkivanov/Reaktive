package com.badoo.gtk3.container

import com.badoo.gtk3.widget.UiWidget

interface UiContainer : UiWidget {

    operator fun <T : UiWidget> T.unaryPlus(): T
}
