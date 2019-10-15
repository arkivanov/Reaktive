package com.badoo.reaktive.disposable

import kotlin.jvm.Volatile

actual class CompositeDisposable actual constructor() : Disposable {

    @Volatile
    private var list: MutableList<Disposable>? = ArrayList()
    override val isDisposed: Boolean get() = list == null

    override fun dispose() {
        val listToDispose: List<Disposable>?

        synchronized(this) {
            listToDispose = list
            list = null
        }

        listToDispose?.forEach(Disposable::dispose)
    }

    actual fun add(disposable: Disposable) {
        synchronized(this) {
            list?.apply {
                add(disposable)
                return
            }
        }

        disposable.dispose()
    }

    actual operator fun plusAssign(disposable: Disposable) {
        add(disposable)
    }

    actual fun clear(dispose: Boolean) {
        val listToDispose: List<Disposable>?

        synchronized(this) {
            listToDispose = list?.takeIf { dispose }
            list = ArrayList()
        }

        listToDispose?.forEach(Disposable::dispose)
    }
}
