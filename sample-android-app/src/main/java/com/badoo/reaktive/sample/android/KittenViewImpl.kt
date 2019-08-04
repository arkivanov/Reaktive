package com.badoo.reaktive.sample.android

import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.samplemppmodule.KittenView
import com.badoo.reaktive.samplemppmodule.KittenView.Event
import com.badoo.reaktive.samplemppmodule.KittenView.ViewModel
import com.badoo.reaktive.subject.publish.publishSubject
import com.squareup.picasso.Picasso

class KittenViewImpl(private val root: View) : KittenView {

    private val _events = publishSubject<Event>()
    override val events: Observable<Event> = _events

    private val image = root.findViewById<ImageView>(R.id.image)
    private val progressBar = root.findViewById<View>(R.id.progress_bar)

    init {
        root.findViewById<View>(R.id.button).setOnClickListener {
            _events.onNext(Event.Reload)
        }
    }

    override fun show(model: ViewModel) {
        model.error?.handle()?.also {
            Toast.makeText(root.context, "Error", Toast.LENGTH_LONG).show()
        }

        Picasso.get().load(model.kittenUrl).into(image)
        progressBar.visibility = if (model.isLoading) View.VISIBLE else View.INVISIBLE
    }
}