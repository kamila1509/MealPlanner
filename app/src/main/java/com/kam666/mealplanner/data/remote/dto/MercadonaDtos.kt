package com.kam666.mealplanner.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MercadonaCategoryResponseDto(
    val id: Int,
    val name: String,
    val categories: List<MercadonaSubcategoryDto> = emptyList()
)

@Serializable
data class MercadonaSubcategoryDto(
    val id: Int,
    val name: String,
    val products: List<MercadonaProductDto> = emptyList()
)

@Serializable
data class MercadonaProductDto(
    val id: String,
    @SerialName("display_name") val displayName: String,
    val thumbnail: String,
    val packaging: String? = null,
    @SerialName("price_instructions") val priceInstructions: MercadonaPriceInstructionsDto
)

@Serializable
data class MercadonaPriceInstructionsDto(
    @SerialName("unit_price") val unitPrice: String,
    @SerialName("bulk_price") val bulkPrice: String? = null,
    @SerialName("unit_size") val unitSize: Double = 1.0,
    @SerialName("size_format") val sizeFormat: String = "ud",
    @SerialName("reference_format") val referenceFormat: String? = null
)

// Algolia search
@Serializable
data class AlgoliaSearchRequestDto(
    val query: String,
    val hitsPerPage: Int = 5,
    val analyticsTags: List<String> = listOf("web"),
    val analytics: Boolean = true
)

@Serializable
data class AlgoliaSearchResponseDto(
    val hits: List<MercadonaProductDto> = emptyList(),
    val nbHits: Int = 0
)
