package com.badoo.reaktive.samplemppmodule

import com.badoo.reaktive.samplemppmodule.KittenStore.State
import com.badoo.reaktive.samplemppmodule.KittenView.ViewModel

internal object KittenStateToViewModelMapper {

    operator fun invoke(state: State): ViewModel =
        ViewModel(
            isLoading = state.isLoading,
            error = state.error,
            kittenUrl = state.kitten?.url
        )
}