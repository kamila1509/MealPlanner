package com.kam666.mealplanner.data.repository

import com.kam666.mealplanner.data.remote.MercadonaApiService
import com.kam666.mealplanner.data.remote.dto.MercadonaProductDto
import com.kam666.mealplanner.domain.repository.MercadonaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MercadonaRepositoryImpl @Inject constructor(
    private val apiService: MercadonaApiService
) : MercadonaRepository {

    private val categoryCache = mutableMapOf<Int, List<MercadonaProductDto>>()
    private val searchCache = mutableMapOf<String, List<MercadonaProductDto>>()

    override suspend fun getProductsForCategoryIds(categoryIds: List<Int>): List<MercadonaProductDto> {
        val uncached = categoryIds.filter { it !in categoryCache }
        if (uncached.isNotEmpty()) {
            coroutineScope {
                uncached.map { id ->
                    async {
                        runCatching { apiService.getCategoryDetail(id) }
                            .getOrNull()
                            ?.let { response ->
                                categoryCache[id] = response.categories.flatMap { it.products }
                            }
                    }
                }.forEach { it.await() }
            }
        }
        return categoryIds.flatMap { categoryCache[it] ?: emptyList() }
    }

    override suspend fun searchProducts(query: String, hitsPerPage: Int): List<MercadonaProductDto> {
        val cacheKey = "$query:$hitsPerPage"
        searchCache[cacheKey]?.let { return it }
        val result = runCatching { apiService.searchProducts(query, hitsPerPage) }
            .getOrNull()?.hits ?: emptyList()
        searchCache[cacheKey] = result
        return result
    }
}
