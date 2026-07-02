package com.example.proyectofinalmoviles1.data.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

class MatchResponseDeserializer : JsonDeserializer<MatchResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): MatchResponse {
        val obj = json.asJsonObject
        return MatchResponse(
            id = obj.intValue("id"),
            home_team = obj.stringValue("home_team"),
            away_team = obj.stringValue("away_team"),
            match_date = obj.stringValue("match_date"),
            phase = obj.stringValue("phase"),
            group_name = obj.nullableStringValue("group_name"),
            status = obj.stringValue("status"),
            home_score = obj.nullableIntValue("home_score"),
            away_score = obj.nullableIntValue("away_score"),
            stadium = obj.stadiumName()
        )
    }

    private fun JsonObject.stadiumName(): String? {
        val element = get("stadium") ?: return null
        if (element.isJsonNull) return null
        if (element.isJsonPrimitive) return element.asString
        if (!element.isJsonObject) return null

        val stadium = element.asJsonObject
        return stadium.nullableStringValue("name")
            ?: stadium.nullableStringValue("stadium_name")
            ?: stadium.nullableStringValue("nombre")
            ?: stadium.nullableStringValue("city")
    }

    private fun JsonObject.stringValue(name: String): String = nullableStringValue(name).orEmpty()

    private fun JsonObject.nullableStringValue(name: String): String? {
        val element = get(name) ?: return null
        if (element.isJsonNull) return null
        return when {
            element.isJsonPrimitive -> element.asString
            else -> element.toString()
        }
    }

    private fun JsonObject.intValue(name: String): Int = nullableIntValue(name) ?: 0

    private fun JsonObject.nullableIntValue(name: String): Int? {
        val element = get(name) ?: return null
        if (element.isJsonNull) return null
        return when {
            element.isJsonPrimitive && element.asJsonPrimitive.isNumber -> element.asInt
            element.isJsonPrimitive -> element.asString.toIntOrNull()
            else -> null
        }
    }
}
