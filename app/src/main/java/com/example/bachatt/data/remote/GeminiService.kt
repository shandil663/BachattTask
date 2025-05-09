package com.example.bachatt.data.remote
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val api: GeminiApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeminiApi::class.java)
}
