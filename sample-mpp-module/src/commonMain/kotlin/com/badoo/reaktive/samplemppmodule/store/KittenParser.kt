package com.badoo.reaktive.samplemppmodule.store

internal class KittenParser {

    private val regex = "(?:\"url\":\")(.*?)(?:\")".toRegex()

    fun parseUrl(json: String): String = regex.find(json)!!.groupValues[1]
}