package com.badoo.reaktive.samplemppmodule

import com.badoo.reaktive.observable.Observable

interface KittenView {

    val events: Observable<Event>

    fun show(model: ViewModel)

    data class ViewModel(
        val isLoading: Boolean,
        val error: SingleLifeEvent<Unit>?,
        val kittenUrl: String?
    )

    sealed class Event {
        object Reload : Event()
    }
}