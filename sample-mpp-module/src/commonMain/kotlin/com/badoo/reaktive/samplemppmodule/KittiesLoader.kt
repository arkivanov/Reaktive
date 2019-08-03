package com.badoo.reaktive.samplemppmodule

import com.badoo.reaktive.annotations.EventsOnAnyScheduler
import com.badoo.reaktive.single.Single

internal interface KittiesLoader {

    @EventsOnAnyScheduler
    fun load(): Single<Result>

    sealed class Result {
        class Success(val kitties: List<Kittie>) : Result()
        object Error : Result()
    }
}