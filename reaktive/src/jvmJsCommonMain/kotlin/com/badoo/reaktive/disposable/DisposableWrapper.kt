package com.badoo.reaktive.disposable

import kotlin.jvm.Volatile

actual class DisposableWrapper actual constructor() : Disposable {

    @Volatile
    private var _isDisposed: Boolean = false
    override val isDisposed: Boolean get() = _isDisposed
    private var disposable: Disposable? = null

    override fun dispose() {
        val disposableToDispose: Disposable?

        synchronized(this) {
            _isDisposed = true
            disposableToDispose = disposable
            disposable = null
        }

        disposableToDispose?.dispose()
    }

    actual fun set(disposable: Disposable?) {
        val disposableToDispose: Disposable?

        synchronized(this) {
            if (_isDisposed) {
                disposableToDispose = disposable
            } else {
                disposableToDispose = this.disposable
                this.disposable = disposable
            }
        }

        disposableToDispose?.dispose()
    }
}
