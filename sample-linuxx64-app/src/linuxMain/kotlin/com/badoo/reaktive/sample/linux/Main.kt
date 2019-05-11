package com.badoo.reaktive.sample.linux

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.flowable.FlowableObserver
import com.badoo.reaktive.flowable.FlowableValue
import com.badoo.reaktive.flowable.flatMap
import com.badoo.reaktive.flowable.flowable
import com.badoo.reaktive.flowable.observeOn
import com.badoo.reaktive.flowable.subscribeOn
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.scheduler.mainScheduler
import platform.posix.pthread_cond_signal
import platform.posix.sleep
import platform.posix.usleep
import kotlin.system.getTimeMillis

/**
 * How to run: execute ":sample-linuxx64-app:runDebugExecutableLinux" Gradle task
 */
fun main() {

    flowable<Int> { emitter ->
        repeat(5) {
            println("Before onNext: $it")
            emitter.onNext(it)
            println("After onNext: $it")
        }
        emitter.onComplete()
    }
        .subscribeOn(ioScheduler)
        .flatMap { value ->
            flowable<String> { emitter ->
                repeat(5) {
                    val v = "$value,$it"
                    println("Before onNext: $v")
                    emitter.onNext(v)
                    println("After onNext: $v")
                }
                emitter.onComplete()
            }
                .subscribeOn(ioScheduler)
        }
        .observeOn(ioScheduler)

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
                    println("${getTimeMillis()}: Received ${value.value}")
                    sleepMs(100L)
                    println("${getTimeMillis()}: Processed ${value.value}")
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