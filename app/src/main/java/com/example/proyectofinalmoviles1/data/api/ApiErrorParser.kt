package com.example.proyectofinalmoviles1.data.api

import com.google.gson.GsonBuilder
import retrofit2.Response

fun parseApiError(response: Response<*>, defaultMessage: String): String {
    return try {
        val errorBody = response.errorBody()?.string()
        if (errorBody.isNullOrBlank()) return "$defaultMessage (${response.code()})"

        val trimmed = errorBody.trimStart()
        if (trimmed.startsWith("<")) {
            return "La API devolvio HTML en lugar de JSON (${response.code()})"
        }
        if (!trimmed.startsWith("{")) return "$defaultMessage (${response.code()})"

        val gson = GsonBuilder().setLenient().create()
        gson.fromJson(errorBody, ErrorResponse::class.java)?.message ?: "$defaultMessage (${response.code()})"
    } catch (_: Exception) {
        "$defaultMessage (${response.code()})"
    }
}

fun apiExceptionMessage(exception: Throwable, defaultMessage: String): String {
    val message = exception.message.orEmpty()
    return when {
        message.contains("Use JsonReader.setLenient", ignoreCase = true) ->
            "La API devolvio una respuesta no JSON. Verifica autenticacion o servidor."
        message.contains("Expected BEGIN_OBJECT", ignoreCase = true) ->
            "La API devolvio un formato distinto al esperado."
        message.contains("Expected BEGIN_ARRAY", ignoreCase = true) ->
            "La API devolvio un formato distinto al esperado."
        message.contains("HTML", ignoreCase = true) ->
            "La API devolvio HTML en lugar de JSON."
        message.isNotBlank() -> message
        else -> defaultMessage
    }
}
