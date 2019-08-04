package com.badoo.reaktive.samplemppmodule

import com.badoo.reaktive.samplemppmodule.KittenStore.Intent
import com.badoo.reaktive.samplemppmodule.KittenView.Event

internal object KittenViewEventToIntentMapper {

    operator fun invoke(event: Event): Intent =
        when (event) {
            is Event.Reload -> Intent.Reload
        }
}