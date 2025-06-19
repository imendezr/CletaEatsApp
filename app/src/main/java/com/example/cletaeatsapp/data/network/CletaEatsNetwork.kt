package com.example.cletaeatsapp.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

object CletaEatsNetwork {
    private const val BASE_URL = "http://10.0.2.2:8080/" // Para emulador Android

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Timber.d(message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val moshi = MoshiAdapters.createMoshi()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: CletaEatsApiService = retrofit.create(CletaEatsApiService::class.java)
}
