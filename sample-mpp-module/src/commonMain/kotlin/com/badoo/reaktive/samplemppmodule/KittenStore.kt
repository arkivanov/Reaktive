package com.badoo.reaktive.samplemppmodule

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable

interface KittenStore : Disposable {

    val states: Observable<State>

    fun accept(intent: Intent)

    data class State(
        val isLoading: Boolean = false,
        val error: SingleLifeEvent<Unit>? = null,
        val kitten: Kitten? = null
    )

    sealed class Intent {
        object Reload : Intent()
    }
}

