package com.kam666.mealplanner.domain.usecase.shoppinglist

import com.kam666.mealplanner.domain.model.ShoppingListItem
import com.kam666.mealplanner.domain.repository.MealPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GenerateShoppingListUseCase @Inject constructor(
    private val repository: MealPlanRepository
) {
    operator fun invoke(weekStart: LocalDate): Flow<List<ShoppingListItem>> =
        repository.getForWeek(weekStart).map { mealPlans ->
            val totals = mutableMapOf<Long, Pair<com.kam666.mealplanner.domain.model.Ingredient, Double>>()
            for (plan in mealPlans) {
                val scaleFactor = plan.peopleCount.toDouble() / plan.recipe.servings
                for (ri in plan.recipe.ingredients) {
                    val scaledQty = ri.quantity * scaleFactor
                    val existing = totals[ri.ingredientId]
                    totals[ri.ingredientId] = if (existing == null) {
                        Pair(ri.ingredient, scaledQty)
                    } else {
                        Pair(existing.first, existing.second + scaledQty)
                    }
                }
            }
            totals.values
                .map { (ingredient, qty) -> ShoppingListItem(ingredient, qty) }
                .sortedBy { it.ingredient.name }
        }
}
