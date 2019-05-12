package com.badoo.reaktive.sample.android

import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.flowable.FlowableObserver
import com.badoo.reaktive.flowable.FlowableValue
import com.badoo.reaktive.flowable.flatMap
import com.badoo.reaktive.flowable.flowable
import com.badoo.reaktive.flowable.observeOn
import com.badoo.reaktive.flowable.subscribeOn
import com.badoo.reaktive.observable.ObservableObserver
import com.badoo.reaktive.observable.flatMap
import com.badoo.reaktive.observable.observable
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.subscribeOn
import com.badoo.reaktive.scheduler.ioScheduler

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.text)

        findViewById<Button>(R.id.button).setOnClickListener {
            Log.v("MyTest", "started")
            val startTime = SystemClock.uptimeMillis()

            observable<Int> { emitter ->
                repeat(100) {
                    emitter.onNext(it)
                }
                emitter.onComplete()
            }
                .subscribeOn(ioScheduler)
                .flatMap { index ->
                    observable<String> { emitter ->
                        repeat(100) {
                            emitter.onNext("$index,$it")
                        }
                        emitter.onComplete()
                    }
                        .subscribeOn(ioScheduler)
                }
                .observeOn(ioScheduler)
                .subscribe(
                    object : ObservableObserver<String> {
                        override fun onSubscribe(disposable: Disposable) {
                        }

                        override fun onNext(value: String) {
                        }

                        override fun onComplete() {
                            Log.v("MyTest", "finished: ${SystemClock.uptimeMillis() - startTime}")
                        }

                        override fun onError(error: Throwable) {
                        }
                    }
                )

//            Flowable
//                .create<Int>(
//                    { emitter ->
//                        repeat(100) {
//                            emitter.onNext(it)
//                        }
//                        emitter.onComplete()
//                    },
//                    BackpressureStrategy.BUFFER
//                )
//                .subscribeOn(Schedulers.io())
//                .flatMap { index ->
//                    Flowable
//                        .create<String>(
//                            { emitter ->
//                                repeat(1000) {
//                                    emitter.onNext("$index,$it")
//                                }
//                                emitter.onComplete()
//                            },
//                            BackpressureStrategy.BUFFER
//                        )
//                        .subscribeOn(Schedulers.io())
//                }
//                .observeOn(Schedulers.io())
//                .subscribe(
//                    object : FlowableSubscriber<String> {
//                        private lateinit var sub: Subscription
//
//                        override fun onSubscribe(s: Subscription) {
//                            sub = s
//                            s.request(1L)
//                        }
//
//                        override fun onComplete() {
//                            Log.v("MyTest", "finished: ${SystemClock.uptimeMillis() - startTime}")
//                        }
//
//                        override fun onNext(t: String) {
//                            sub.request(1L)
//                        }
//
//                        override fun onError(t: Throwable?) {
//                        }
//                    }
//                )

//            observable<String> { emitter ->
//                emitter.onNext("Loading...")
//                Thread.sleep(1000L)
//                emitter.onNext(SimpleDateFormat.getDateTimeInstance().format(Date()))
//                emitter.onComplete()
//            }
//                .subscribeOn(ioScheduler)
//                .observeOn(mainScheduler)
//                .subscribe(onNext = textView::setText)
        }
    }
}
