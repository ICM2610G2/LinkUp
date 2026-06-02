package com.example.myapplication.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface WikipediaApiService {

    @GET("api/rest_v1/page/summary/{title}")
    suspend fun getPlaceSummary(
        @Path("title") title: String
    ): WikipediaSummaryDto
}