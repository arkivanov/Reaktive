package sample.libcurl

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.get
import kotlinx.cinterop.staticCFunction
import libcurl.CURLE_OK
import libcurl.CURLOPT_URL
import libcurl.CURLOPT_WRITEDATA
import libcurl.CURLOPT_WRITEFUNCTION
import libcurl.curl_easy_init
import libcurl.curl_easy_perform
import libcurl.curl_easy_setopt
import platform.posix.size_t

fun curl(url: String): ByteArray? {
    val curl = curl_easy_init()
    try {
        curl_easy_setopt(curl, CURLOPT_URL, url)
        val data = arrayListOf<Byte>()
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, staticCFunction(::writeCallback))
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, StableRef.create(data).asCPointer())

        if (curl_easy_perform(curl) != CURLE_OK) {
            return null
        }

        return ByteArray(data.size, data::get)
    } finally {
//        cu
    }
}

private fun writeCallback(buffer: CPointer<ByteVar>?, itemSize: size_t, itemCount: size_t, userData: COpaquePointer?): size_t {
    if (buffer == null) {
        return 0U
    }

    val data = userData?.asStableRef<MutableList<Byte>>()?.get()
    if (data != null) {
        for (i in 0 until itemCount.toInt()) {
            data += buffer[i]
        }
    }

    return itemSize * itemCount
}