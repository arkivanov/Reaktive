package com.badoo.reaktive.disposable

interface DisposableConsumer {

    fun accept(disposable: Disposable)
}