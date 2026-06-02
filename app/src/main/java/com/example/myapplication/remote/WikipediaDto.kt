package com.example.myapplication.remote

data class WikipediaSummaryDto(
    val title: String? = null,
    val extract: String? = null,
    val thumbnail: WikipediaThumbnailDto? = null,
    val content_urls: WikipediaContentUrlsDto? = null
)

data class WikipediaThumbnailDto(
    val source: String? = null
)

data class WikipediaContentUrlsDto(
    val desktop: WikipediaDesktopUrlDto? = null
)

data class WikipediaDesktopUrlDto(
    val page: String? = null
)