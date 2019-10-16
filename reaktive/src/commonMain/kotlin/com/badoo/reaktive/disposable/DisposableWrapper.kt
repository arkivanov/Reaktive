package com.badoo.reaktive.disposable

/**
 * Thread-safe container of one [Disposable]
 */
expect class DisposableWrapper() : DisposableContainer {

    /**
     * Atomically either replaces any existing [Disposable] with the specified one or disposes it if wrapper is already disposed.
     * Also disposes any replaced [Disposable].
     */
    fun set(disposable: Disposable?)
}
