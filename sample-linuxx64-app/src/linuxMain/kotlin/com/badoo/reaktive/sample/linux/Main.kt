package com.badoo.reaktive.sample.linux

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.flowable.BackPressureStrategy
import com.badoo.reaktive.flowable.Flowable
import com.badoo.reaktive.flowable.FlowableObserver
import com.badoo.reaktive.flowable.FlowableValue
import com.badoo.reaktive.flowable.combineLatest
import com.badoo.reaktive.flowable.flowable
import com.badoo.reaktive.flowable.observeOn
import com.badoo.reaktive.flowable.subscribeOn
import com.badoo.reaktive.scheduler.ioScheduler
import platform.posix.sleep
import platform.posix.usleep
import kotlin.system.getTimeMillis

/**
 * How to run: execute ":sample-linuxx64-app:runDebugExecutableLinux" Gradle task
 */
fun main() {

    println("start")
    List(100) {index ->
        List(10) { "$index,$it" }.asFlowable()
    }
        .combineLatest { it.joinToString(separator = ";") }
        .observeOn(ioScheduler, BackPressureStrategy(bufferSize = 0, overflowStrategy = BackPressureStrategy.OverflowStrategy.DROP_OLDEST))

//    flowable<Int> { emitter ->
//        repeat(5) {
//            println("Before onNext: $it")
//            emitter.onNext(it)
//            println("After onNext: $it")
//        }
//        emitter.onComplete()
//    }
//        .observeOn(mainScheduler)
        .subscribe(
            object : FlowableObserver<String> {
                override fun onSubscribe(disposable: Disposable) {
                }

                override fun onNext(value: FlowableValue<String>) {
//                    println("${getTimeMillis()}: Received ${value.value}")
//                    sleepMs(10L)
//                    println("${getTimeMillis()}: Processed ${value.value}")
                    value.onProcessed()
                }

                override fun onComplete() {
                    println("complete")
                }

                override fun onError(error: Throwable) {
                }
            }
        )

    sleep(6)
}

fun sleepMs(millis: Long) {
    usleep((millis * 1000L).toUInt())
}

fun <T> Iterable<T>.asFlowable(): Flowable<T> =
    flowable<T> { emitter ->
        forEach {
//            println("Before onNext: $it")
            emitter.onNext(it)
//            println("After onNext: $it")
        }
        emitter.onComplete()
    }
        .subscribeOn(ioScheduler)