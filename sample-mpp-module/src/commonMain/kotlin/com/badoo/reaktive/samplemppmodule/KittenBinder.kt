package com.badoo.reaktive.samplemppmodule

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.subscribe

class KittenBinder(
    storeBuilder: KittenStoreBuilder
) {

    private var disposables = CompositeDisposable()
    private val store = storeBuilder.build()
    private var view: KittenView? = null

    fun onViewCreated(view: KittenView) {
        this.view = view
    }

    fun onStart() {
        disposables +=
            view!!
                .events
                .map(KittenViewEventToIntentMapper::invoke)
                .subscribe(onNext = store::accept)

        disposables +=
            store
                .states
                .map(KittenStateToViewModelMapper::invoke)
                .subscribe(onNext = { view!!.show(it) })
    }

    fun onStop() {
        disposables.clear()
    }

    fun onViewDestroyed() {
        view = null
    }

    fun onDestroy() {
        store.dispose()
    }
}