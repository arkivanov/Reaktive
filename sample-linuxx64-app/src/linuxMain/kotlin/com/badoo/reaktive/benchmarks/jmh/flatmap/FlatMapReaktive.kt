package com.badoo.reaktive.benchmarks.jmh.flatmap

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.asObservable
import com.badoo.reaktive.observable.flatMap
import com.badoo.reaktive.observable.observable
import com.badoo.reaktive.observable.subscribe

open class FlatMapReaktive {

    fun iterable() {
        FlatMapConfig
            .input
            .asObservable()
            .also(::run)
    }

    fun emitter() {
        observable<Int> { emitter ->
            FlatMapConfig.input.forEach(emitter::onNext)
            emitter.onComplete()
        }
            .also(::run)
    }

    private fun run(input: Observable<Int>) {
        input
            .flatMap { input }
            .subscribe()
    }
}
