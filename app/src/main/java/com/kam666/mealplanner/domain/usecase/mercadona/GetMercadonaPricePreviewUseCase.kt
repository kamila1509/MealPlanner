package com.kam666.mealplanner.domain.usecase.mercadona

import com.kam666.mealplanner.domain.model.PriceMatchResult
import com.kam666.mealplanner.domain.usecase.shoppinglist.GenerateShoppingListUseCase
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class GetMercadonaPricePreviewUseCase @Inject constructor(
    private val generateShoppingList: GenerateShoppingListUseCase,
    private val matchMercadonaProducts: MatchMercadonaProductsUseCase
) {
    suspend operator fun invoke(weekStart: LocalDate): List<PriceMatchResult> {
        val shoppingItems = generateShoppingList(weekStart).first()
        return matchMercadonaProducts(shoppingItems)
    }
}
