package com.badoo.reaktive.flowable

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.utils.Uninitialized
import com.badoo.reaktive.utils.atomicreference.AtomicReference
import com.badoo.reaktive.utils.atomicreference.update
import com.badoo.reaktive.utils.atomicreference.updateAndGet
import com.badoo.reaktive.utils.replace
import com.badoo.reaktive.utils.serializer.serializer

fun <T, R> Collection<Flowable<T>>.combineLatest(mapper: (List<T>) -> R): Flowable<R> =
    flowableSafe { observer ->
        val disposables = CompositeDisposable()
        observer.onSubscribe(disposables)
        val values = AtomicReference<List<Any?>>(List(size) { Uninitialized }, true)
        val pendingValues = AtomicReference<List<FlowableValue<T>>?>(listOf(), true)
        val activeSourceCount = AtomicReference(size)

        @Suppress("UNCHECKED_CAST") val serializer =
            serializer<CombineLatestEvent<T>> { event ->
                when (event) {
                    is CombineLatestEvent.OnNext -> {
                        val newValues: List<Any?>? =
                            values
                                .updateAndGet { it.replace(event.index, event.value.value) }
                                .takeIf { newValues -> newValues.none { it === Uninitialized } }

                        if (newValues == null) {
                            pendingValues.update {
                                it?.plus(event.value)
                            }
                        } else {
                            val mappedValue =
                                try {
                                    @Suppress("UNCHECKED_CAST")
                                    mapper(newValues as List<T>)
                                } catch (e: Throwable) {
                                    observer.onError(e)
                                    return@serializer false
                                }

//                            observer.onNextBlocking(mappedValue)

                            pendingValues
                                .value
                                ?.forEach(FlowableValue<T>::onProcessed)
                                ?.also { pendingValues.value = null }

                            event.value.onProcessed()
                        }

                        true
                    }

                    is CombineLatestEvent.OnComplete -> {
                        val remainingActiveSources = activeSourceCount.updateAndGet { it - 1 }

                        // Complete if all sources are completed or a source is completed without a value
                        val allCompleted = (remainingActiveSources == 0) || (values.value[event.index] === Uninitialized)
                        if (allCompleted) {
                            observer.onComplete()
                        }

                        !allCompleted
                    }

                    is CombineLatestEvent.OnError -> {
                        observer.onError(event.error)
                        false
                    }
                }
            }

        forEachIndexed { index, source ->
            source.subscribeSafe(
                object : FlowableObserver<T> {
                    override fun onSubscribe(disposable: Disposable) {
                        disposables += disposable
                    }

                    override fun onNext(value: FlowableValue<T>) {
                        serializer.accept(CombineLatestEvent.OnNext(index, value))
                    }

                    override fun onComplete() {
                        serializer.accept(CombineLatestEvent.OnComplete(index))
                    }

                    override fun onError(error: Throwable) {
                        serializer.accept(CombineLatestEvent.OnError(error))
                    }
                }
            )
        }
    }

private sealed class CombineLatestEvent<out T> {

    class OnNext<out T>(val index: Int, val value: FlowableValue<T>) : CombineLatestEvent<T>()
    class OnComplete(val index: Int) : CombineLatestEvent<Nothing>()
    class OnError(val error: Throwable) : CombineLatestEvent<Nothing>()
}

fun <T, R> combineLatest(vararg sources: Flowable<T>, mapper: (List<T>) -> R): Flowable<R> =
    sources
        .toList()
        .combineLatest(mapper)

fun <T1, T2, R> combineLatest(
    source1: Flowable<T1>,
    source2: Flowable<T2>,
    mapper: (T1, T2) -> R
): Flowable<R> =
    listOf(source1, source2)
        .combineLatest { values ->
            @Suppress("UNCHECKED_CAST")
            mapper(values[0] as T1, values[1] as T2)
        }

fun <T1, T2, T3, R> combineLatest(
    source1: Flowable<T1>,
    source2: Flowable<T2>,
    source3: Flowable<T3>,
    mapper: (T1, T2, T3) -> R
): Flowable<R> =
    listOf(source1, source2, source3)
        .combineLatest { values ->
            @Suppress("UNCHECKED_CAST")
            mapper(values[0] as T1, values[1] as T2, values[2] as T3)
        }

fun <T1, T2, T3, T4, R> combineLatest(
    source1: Flowable<T1>,
    source2: Flowable<T2>,
    source3: Flowable<T3>,
    source4: Flowable<T4>,
    mapper: (T1, T2, T3, T4) -> R
): Flowable<R> =
    listOf(source1, source2, source3, source4)
        .combineLatest { values ->
            @Suppress("UNCHECKED_CAST")
            mapper(values[0] as T1, values[1] as T2, values[2] as T3, values[3] as T4)
        }

fun <T1, T2, T3, T4, T5, R> combineLatest(
    source1: Flowable<T1>,
    source2: Flowable<T2>,
    source3: Flowable<T3>,
    source4: Flowable<T4>,
    source5: Flowable<T5>,
    mapper: (T1, T2, T3, T4, T5) -> R
): Flowable<R> =
    listOf(source1, source2, source3, source4, source5)
        .combineLatest { values ->
            @Suppress("UNCHECKED_CAST")
            mapper(values[0] as T1, values[1] as T2, values[2] as T3, values[3] as T4, values[4] as T5)
        }

fun <T1, T2, T3, T4, T5, T6, R> combineLatest(
    source1: Flowable<T1>,
    source2: Flowable<T2>,
    source3: Flowable<T3>,
    source4: Flowable<T4>,
    source5: Flowable<T5>,
    source6: Flowable<T6>,
    mapper: (T1, T2, T3, T4, T5, T6) -> R
): Flowable<R> =
    listOf(source1, source2, source3, source4, source5, source6)
        .combineLatest { values ->
            @Suppress("UNCHECKED_CAST")
            mapper(values[0] as T1, values[1] as T2, values[2] as T3, values[3] as T4, values[4] as T5, values[5] as T6)
        }

fun <T1, T2, T3, T4, T5, T6, T7, R> combineLatest(
    source1: Flowable<T1>,
    source2: Flowable<T2>,
    source3: Flowable<T3>,
    source4: Flowable<T4>,
    source5: Flowable<T5>,
    source6: Flowable<T6>,
    source7: Flowable<T7>,
    mapper: (T1, T2, T3, T4, T5, T6, T7) -> R
): Flowable<R> =
    listOf(source1, source2, source3, source4, source5, source6, source7)
        .combineLatest { values ->
            @Suppress("UNCHECKED_CAST")
            mapper(
                values[0] as T1,
                values[1] as T2,
                values[2] as T3,
                values[3] as T4,
                values[4] as T5,
                values[5] as T6,
                values[6] as T7
            )
        }

fun <T1, T2, T3, T4, T5, T6, T7, T8, R> combineLatest(
    source1: Flowable<T1>,
    source2: Flowable<T2>,
    source3: Flowable<T3>,
    source4: Flowable<T4>,
    source5: Flowable<T5>,
    source6: Flowable<T6>,
    source7: Flowable<T7>,
    source8: Flowable<T8>,
    mapper: (T1, T2, T3, T4, T5, T6, T7, T8) -> R
): Flowable<R> =
    listOf(source1, source2, source3, source4, source5, source6, source7, source8)
        .combineLatest { values ->
            @Suppress("UNCHECKED_CAST")
            mapper(
                values[0] as T1,
                values[1] as T2,
                values[2] as T3,
                values[3] as T4,
                values[4] as T5,
                values[5] as T6,
                values[6] as T7,
                values[7] as T8
            )
        }

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> combineLatest(
    source1: Flowable<T1>,
    source2: Flowable<T2>,
    source3: Flowable<T3>,
    source4: Flowable<T4>,
    source5: Flowable<T5>,
    source6: Flowable<T6>,
    source7: Flowable<T7>,
    source8: Flowable<T8>,
    source9: Flowable<T9>,
    mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R
): Flowable<R> =
    listOf(source1, source2, source3, source4, source5, source6, source7, source8, source9)
        .combineLatest { values ->
            @Suppress("UNCHECKED_CAST")
            mapper(
                values[0] as T1,
                values[1] as T2,
                values[2] as T3,
                values[3] as T4,
                values[4] as T5,
                values[5] as T6,
                values[6] as T7,
                values[7] as T8,
                values[8] as T9
            )
        }

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> combineLatest(
    source1: Flowable<T1>,
    source2: Flowable<T2>,
    source3: Flowable<T3>,
    source4: Flowable<T4>,
    source5: Flowable<T5>,
    source6: Flowable<T6>,
    source7: Flowable<T7>,
    source8: Flowable<T8>,
    source9: Flowable<T9>,
    source10: Flowable<T10>,
    mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> R
): Flowable<R> =
    listOf(source1, source2, source3, source4, source5, source6, source7, source8, source9, source10)
        .combineLatest { values ->
            @Suppress("UNCHECKED_CAST")
            mapper(
                values[0] as T1,
                values[1] as T2,
                values[2] as T3,
                values[3] as T4,
                values[4] as T5,
                values[5] as T6,
                values[6] as T7,
                values[7] as T8,
                values[8] as T9,
                values[9] as T10
            )
        }