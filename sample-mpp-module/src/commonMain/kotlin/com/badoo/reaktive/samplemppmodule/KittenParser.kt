package com.badoo.reaktive.samplemppmodule

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject

internal class KittenParser {

    private val parser = Json(JsonConfiguration.Stable)

    fun parse(json: String): Kitten {
        val jsonObject: JsonObject = parser.parseJson(json).jsonArray[0].jsonObject

        return Kitten(
            url = jsonObject.getPrimitive("url").content
        )
    }
}