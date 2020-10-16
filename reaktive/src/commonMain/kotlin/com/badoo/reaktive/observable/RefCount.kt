package com.badoo.reaktive.observable

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.plusAssign
import com.badoo.reaktive.utils.atomics.addAndGet
import com.badoo.reaktive.utils.atomics.atomic
import com.badoo.reaktive.utils.atomics.getAndChange
import com.badoo.reaktive.utils.atomics.value

fun <T> ConnectableObservable<T>.refCount(subscriberCount: Int = 1): Observable<T> {
    require(subscriberCount > 0)

    val subscribeCount = atomic(0)
    val disposable = atomic<Disposable?>(null)

    return observable { emitter ->
        val disposables = CompositeDisposable()
        emitter.setDisposable(disposables)

        disposables +=
            Disposable {
                if (subscribeCount.addAndGet(-1) == 0) {
                    disposable
                        .getAndChange { null }
                        ?.dispose()
                }
            }

        this@refCount.subscribe(
            object : ObservableObserver<T>, ObservableCallbacks<T> by emitter {
                override fun onSubscribe(disposable: Disposable) {
                    disposables += disposable
                }
            }
        )

        if (subscribeCount.addAndGet(1) == subscriberCount) {
            this@refCount.connect {
                disposable.value = it
            }
        }
    }
}
