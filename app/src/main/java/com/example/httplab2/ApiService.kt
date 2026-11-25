package com.example.httplab2

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): Response<Post>
}