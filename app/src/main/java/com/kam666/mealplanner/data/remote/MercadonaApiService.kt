package com.kam666.mealplanner.data.remote

import com.kam666.mealplanner.data.remote.dto.AlgoliaSearchRequestDto
import com.kam666.mealplanner.data.remote.dto.AlgoliaSearchResponseDto
import com.kam666.mealplanner.data.remote.dto.MercadonaCategoryResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val CATEGORIES_BASE_URL = "https://tienda.mercadona.es/api/categories/"
private const val ALGOLIA_URL =
    "https://7uzjkl1dj0-2.algolianet.com/1/indexes/products_prod_bcn1_es/query" +
    "?x-algolia-agent=Algolia%20for%20JavaScript%20(5.54.1)%3B%20Search%20(5.54.1)%3B%20Browser" +
    "&x-algolia-api-key=9d8f2e39e90df472b4f2e559a116fe17" +
    "&x-algolia-application-id=7UZJKL1DJ0"

private const val USER_AGENT =
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36"

private val requestJson = Json { encodeDefaults = true }

class MercadonaApiService(private val client: HttpClient) {

    suspend fun getCategoryDetail(categoryId: Int): MercadonaCategoryResponseDto =
        client.get("$CATEGORIES_BASE_URL$categoryId/") {
            header("User-Agent", USER_AGENT)
        }.body()

    suspend fun searchProducts(query: String, hitsPerPage: Int = 5): AlgoliaSearchResponseDto =
        client.post(ALGOLIA_URL) {
            header("User-Agent", USER_AGENT)
            header("Origin", "https://tienda.mercadona.es")
            header("Referer", "https://tienda.mercadona.es/")
            contentType(ContentType.Text.Plain)
            setBody(
                requestJson.encodeToString(
                    AlgoliaSearchRequestDto(query = query, hitsPerPage = hitsPerPage)
                )
            )
        }.body()
}
