package com.example.maptest.geoAPI

import com.example.maptest.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val retrofit: Retrofit by lazy {
    //logs
    val interceptor = HttpLoggingInterceptor()
    interceptor.level =
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE

    //timeouts
    val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    //retrofit
    Retrofit.Builder()
        .baseUrl("https://waadsu.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
}