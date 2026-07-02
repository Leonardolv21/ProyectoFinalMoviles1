package com.example.proyectofinalmoviles1.data.api

import com.example.proyectofinalmoviles1.data.TokenProvider
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://quiniela.jmacboy.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request().newBuilder()
        request.addHeader("Accept", "application/json")
        request.addHeader("X-Requested-With", "XMLHttpRequest")
        TokenProvider.token?.let {
            request.addHeader("Authorization", "Bearer $it")
        }
        chain.proceed(request.build())
    }

    private val htmlResponseInterceptor = okhttp3.Interceptor { chain ->
        val response = chain.proceed(chain.request())
        val body = response.body ?: return@Interceptor response
        val contentType = body.contentType()
        val rawBody = body.string()
        val isHtml = contentType?.toString()?.contains("html", ignoreCase = true) == true ||
            rawBody.trimStart().startsWith("<")

        if (isHtml) {
            val json = """
                {"message":"La API devolvio HTML en lugar de JSON. Verifica la ruta, autenticacion o configuracion del servidor."}
            """.trimIndent()
            response.newBuilder()
                .code(502)
                .message("HTML response from API")
                .body(json.toResponseBody("application/json; charset=utf-8".toMediaType()))
                .build()
        } else {
            response.newBuilder()
                .body(rawBody.toResponseBody(contentType))
                .build()
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(htmlResponseInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .registerTypeAdapter(MatchResponse::class.java, MatchResponseDeserializer())
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
