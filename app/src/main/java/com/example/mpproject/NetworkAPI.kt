package com.example.mpproject

import com.example.mpproject.db.ParliamentMember
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// 27.09.2024 by Arman Yerkeshev 2214297
// This is a singleton class, responsible for sending and receiving HTTP requests
object NetworkAPI {
    private const val BASE_URL = "https://users.metropolia.fi/~hafizs/Kotlin/"
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

// 30.09.2024 by Arman Yerkeshev 2214297
// Defines possible interactions the server
interface ApiService {
    @GET("seating.json")
    fun loadMainData(): Call<List<ParliamentMember>>?

    @GET("extras.json")
    fun loadExtraData(): Call<List<ParliamentMember>>?
}