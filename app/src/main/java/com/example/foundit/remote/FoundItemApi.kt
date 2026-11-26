package com.example.foundit.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FoundItemApi {

    // GET: retrieve test announcements
    @GET("posts")
    suspend fun getItems(): List<FoundItemDto>

    // POST: send a test announcement
    @POST("posts")
    suspend fun createItem(
        @Body item: FoundItemDto
    ): Response<FoundItemDto>
}