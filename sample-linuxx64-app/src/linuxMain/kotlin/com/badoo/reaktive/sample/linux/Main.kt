package com.badoo.reaktive.sample.linux

import com.badoo.reaktive.benchmarks.jmh.concatmap.ConcatMapFlow
import com.badoo.reaktive.benchmarks.jmh.concatmap.ConcatMapReaktive
import com.badoo.reaktive.benchmarks.jmh.filtermap.FilterMapFlow
import com.badoo.reaktive.benchmarks.jmh.filtermap.FilterMapReaktive
import com.badoo.reaktive.benchmarks.jmh.flatmap.FlatMapFlow
import com.badoo.reaktive.benchmarks.jmh.flatmap.FlatMapReaktive
import libgtk3.G_APPLICATION_FLAGS_NONE
import libgtk3.GtkApplication
import libgtk3.g_application_run
import libgtk3.g_object_unref
import libgtk3.gtk_application_new
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * How to run:
 * * Install libcurl4-openssl-dev and libgtk-3-dev in your system
 * * Execute ":sample-linuxx64-app:runDebugExecutableLinux" Gradle task
 */
@OptIn(ExperimentalTime::class)
fun main() {
    // val filterMapFlowEmitter = test { FilterMapFlow().emitter() }
    // val filterMapFlowIterable = test { FilterMapFlow().iterable() }
    // val filterMapReaktiveEmitter = test { FilterMapReaktive().emitter() }
    // val filterMapReaktiveIterable = test { FilterMapReaktive().iterable() }
    //
    // println("filter-map emitter: $filterMapFlowEmitter, $filterMapReaktiveEmitter")
    // println("filter-map iterable: $filterMapFlowIterable, $filterMapReaktiveIterable")

    test { ConcatMapFlow().emitter() }
    test { ConcatMapFlow().iterable() }
    test { ConcatMapReaktive().emitter() }
    test { ConcatMapReaktive().iterable() }

    val concatMapFlowEmitter = test { ConcatMapFlow().emitter() }
    val concatMapFlowIterable = test { ConcatMapFlow().iterable() }
    val concatMapReaktiveEmitter = test { ConcatMapReaktive().emitter() }
    val concatMapReaktiveIterable = test { ConcatMapReaktive().iterable() }
    println("concatMap emitter: $concatMapFlowEmitter, $concatMapReaktiveEmitter")
    println("concatMap iterable: $concatMapFlowIterable, $concatMapReaktiveIterable")

    // val flatMapFlowEmitter = test { FlatMapFlow().emitter() }
    // val flatMapFlowIterable = test { FlatMapFlow().iterable() }
    // val flatMapReaktiveEmitter = test { FlatMapReaktive().emitter() }
    // val flatMapReaktiveIterable = test { FlatMapReaktive().iterable() }
    //
    // println("flatMap emitter: $flatMapFlowEmitter, $flatMapReaktiveEmitter")
    // println("flatMap iterable: $flatMapFlowIterable, $flatMapReaktiveIterable")
}

@OptIn(ExperimentalTime::class)
private fun test(block: () -> Unit): Duration {
    block()
    block()
    block()

    return measureTime {
        block()
        block()
        block()
    } / 3
}
