package com.badoo.reaktive.samplemppmodule

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.samplemppmodule.KittiesStore.Intent
import com.badoo.reaktive.samplemppmodule.KittiesStore.State
import com.badoo.reaktive.scheduler.mainScheduler
import com.badoo.reaktive.single.map
import com.badoo.reaktive.single.observeOn
import com.badoo.reaktive.single.subscribe
import com.badoo.reaktive.subject.behavior.behaviorSubject

internal class KittiesStoreImpl(
    private val loader: KittiesLoader
) : KittiesStore {

    private val _states = behaviorSubject(State())
    override val states: Observable<State> = _states
    private val state: State get() = _states.value

    private val disposables = CompositeDisposable()
    override val isDisposed: Boolean get() = disposables.isDisposed

    override fun dispose() {
        disposables.dispose()
        _states.onComplete()
    }

    override fun accept(intent: Intent) {
        execute(intent)?.also(disposables::add)
    }

    private fun execute(intent: Intent): Disposable? =
        when (intent) {
            is Intent.Reload -> reload()
        }

    private fun reload(): Disposable? =
        if (state.isLoading) {
            null
        } else {
            loader
                .load()
                .map {
                    when (it) {
                        is KittiesLoader.Result.Success -> Result.Loaded(it.kitties)
                        is KittiesLoader.Result.Error -> Result.LoadingFailed
                    }
                }
                .observeOn(mainScheduler)
                .subscribe(onSuccess = ::onResult)
        }

    private fun onResult(result: Result) {
        _states.onNext(Reducer(_states.value, result))
    }

    private sealed class Result {
        object LoadingStarted : Result()
        class Loaded(val kitties: List<Kittie>) : Result()
        object LoadingFailed : Result()
    }

    private object Reducer {
        operator fun invoke(state: State, result: Result): State = TODO()
    }
}