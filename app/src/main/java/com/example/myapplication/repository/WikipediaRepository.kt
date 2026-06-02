package com.example.myapplication.repository

import com.example.myapplication.remote.RetrofitInstance

data class WikipediaPlace(
    val title: String,
    val extract: String,
    val imageUrl: String?,
    val pageUrl: String?
)

class WikipediaRepository {

    suspend fun getPlaceInfo(placeName: String): Result<WikipediaPlace> {
        return try {

            val formattedTitle = placeName
                .trim()
                .replace(" ", "_")

            val response =
                RetrofitInstance.wikipediaApi
                    .getPlaceSummary(formattedTitle)

            Result.success(
                WikipediaPlace(
                    title = response.title ?: placeName,
                    extract = response.extract
                        ?: "No se encontró información para este lugar.",
                    imageUrl = response.thumbnail?.source,
                    pageUrl = response.content_urls?.desktop?.page
                )
            )

        } catch (e: Exception) {

            Result.failure(e)

        }
    }
}