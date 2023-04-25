package com.badoo.reaktive.scheduler

actual fun createTrampolineScheduler(): Scheduler =
    TrampolineScheduler(
        sleep = {
            try {
                Thread.sleep(it.inWholeMilliseconds)
                true
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                false
            }
        }
    )
