package com.badoo.reaktive.samplemppmodule

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable

interface KittiesStore : Disposable {

    val states: Observable<State>

    fun accept(intent: Intent)

    data class State(
        val isLoading: Boolean = false,
        val kitties: List<Kittie> = emptyList()
    )

    sealed class Intent {
        object Reload : Intent()
    }
}