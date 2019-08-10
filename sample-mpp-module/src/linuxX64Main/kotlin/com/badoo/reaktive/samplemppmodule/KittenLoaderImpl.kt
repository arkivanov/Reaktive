package com.badoo.reaktive.samplemppmodule

import com.badoo.reaktive.samplemppmodule.KittenLoader.Result
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.onErrorReturnValue
import com.badoo.reaktive.single.singleFromFunction
import com.badoo.reaktive.single.subscribeOn
import sample.libcurl.curl

internal class KittenLoaderImpl : KittenLoader {

    override fun load(): Single<Result> =
        singleFromFunction {
            val data: ByteArray? = curl(Config.KITTEN_URL)
            if (data == null) {
                Result.Error
            } else {
                Result.Success(data.stringFromUtf8OrThrow())
            }
        }
            .subscribeOn(ioScheduler)
            .onErrorReturnValue(Result.Error)
}