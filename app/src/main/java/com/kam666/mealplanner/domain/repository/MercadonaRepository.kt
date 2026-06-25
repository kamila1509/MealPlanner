package com.kam666.mealplanner.domain.repository

import com.kam666.mealplanner.data.remote.dto.MercadonaProductDto

interface MercadonaRepository {
    suspend fun getProductsForCategoryIds(categoryIds: List<Int>): List<MercadonaProductDto>
    suspend fun searchProducts(query: String, hitsPerPage: Int = 5): List<MercadonaProductDto>
}
