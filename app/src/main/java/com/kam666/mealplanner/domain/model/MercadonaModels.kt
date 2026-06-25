package com.kam666.mealplanner.domain.model

data class MercadonaProduct(
    val id: String,
    val name: String,
    val thumbnailUrl: String,
    val packaging: String,
    val unitPrice: Double,
    val bulkPrice: Double?,
    val unitSize: Double,
    val sizeFormat: String
)

sealed class PriceMatchResult {
    data class Matched(
        val shoppingItem: ShoppingListItem,
        val product: MercadonaProduct,
        val packagesNeeded: Int,
        val estimatedCost: Double,
        val alternatives: List<MercadonaProduct> = emptyList(),
        val requiredInBulkUnit: Double = 0.0,
        val bulkUnit: String = ""
    ) : PriceMatchResult()

    data class NoMatch(
        val shoppingItem: ShoppingListItem
    ) : PriceMatchResult()
}
