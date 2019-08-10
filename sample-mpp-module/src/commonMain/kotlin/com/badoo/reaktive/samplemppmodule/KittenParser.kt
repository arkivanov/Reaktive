package com.badoo.reaktive.samplemppmodule

internal class KittenParser {

    private val regex = "(?:\"url\":\")(.*?)(?:\")".toRegex()

    fun parse(json: String): Kitten = Kitten(url = regex.find(json)!!.groupValues[1])
}