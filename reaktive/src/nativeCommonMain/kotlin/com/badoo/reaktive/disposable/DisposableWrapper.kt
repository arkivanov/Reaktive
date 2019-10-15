package com.badoo.reaktive.disposable

import com.badoo.reaktive.utils.atomic.AtomicReference
import com.badoo.reaktive.utils.atomic.getAndUpdate

actual class DisposableWrapper actual constructor() : Disposable {

    private val ref = AtomicReference<Holder?>(Holder(null))
    override val isDisposed: Boolean get() = ref.value == null

    override fun dispose() {
        setHolder(null)
    }

    actual fun set(disposable: Disposable?) {
        setHolder(Holder(disposable))
    }

    private fun setHolder(holder: Holder?) {
        ref
            .getAndUpdate { oldHolder ->
                if (oldHolder == null) {
                    holder?.dispose()
                }

                holder
            }
            ?.dispose()
    }

    private class Holder(
        private val disposable: Disposable?
    ) {
        fun dispose() {
            disposable?.dispose()
        }
    }
}
