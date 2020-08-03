package com.badoo.reaktive.sample.android

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.badoo.reaktive.observable.*
import com.badoo.reaktive.samplemppmodule.binder.KittenBinder
import com.badoo.reaktive.scheduler.computationScheduler
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.scheduler.mainScheduler
import com.badoo.reaktive.subject.publish.PublishSubject

class MainActivity : AppCompatActivity() {

    private lateinit var kittenBinder: KittenBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // kittenBinder = KittenBinder(KittenStoreBuilderImpl())
        // kittenBinder.onViewCreated(KittenViewImpl(findViewById(android.R.id.content)))

        runBackgroundComputations()
    }

    private fun runBackgroundComputations() {
        observable<Int> { emitter ->
            var value = 0
            while (!emitter.isDisposed) {
                emitter.onNext(value++)
            }
        }
            .doOnBeforeNext { log("Emitting: $it") }
            .doOnAfterNext { log("Emitted: $it") }
            .subscribeOn(ioScheduler)
            .observeOn(computationScheduler, bufferSize = 1, bufferOverflowStrategy = BufferOverflowStrategy.BLOCK)
            .doOnBeforeNext { log("Processing: $it") }
            .map {
                Thread.sleep(500L) // Long running operation
                it
            }
            .doOnAfterNext { log("Processed: $it") }
            .observeOn(mainScheduler)
            .subscribe { log("Received: $it") }
    }

    private fun runButtonClicks() {
        val subject = PublishSubject<Int>()
        var value = 0

        findViewById<View>(R.id.button)
            .setOnClickListener { subject.onNext(value++) }

        subject
            .observeOn(computationScheduler, bufferSize = 3, bufferOverflowStrategy = BufferOverflowStrategy.DROP_OLDEST)
            .map {
                Thread.sleep(1000L)
                it
            }
            .observeOn(mainScheduler)
            .subscribe { log(it.toString()) }
    }

    private fun log(text: String) {
        Log.v("MyTest", text)
    }

    override fun onStart() {
        super.onStart()

        // kittenBinder.onStart()
    }

    override fun onStop() {
        // kittenBinder.onStop()

        super.onStop()
    }

    override fun onDestroy() {
        // kittenBinder.onDestroy()

        super.onDestroy()
    }
}
