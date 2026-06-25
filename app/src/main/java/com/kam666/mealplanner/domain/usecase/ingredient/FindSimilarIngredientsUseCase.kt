package com.kam666.mealplanner.domain.usecase.ingredient

import com.kam666.mealplanner.domain.model.Ingredient
import com.kam666.mealplanner.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FindSimilarIngredientsUseCase @Inject constructor(
    private val repository: IngredientRepository
) {
    operator fun invoke(query: String): Flow<List<Ingredient>> {
        if (query.trim().length < 2) return flowOf(emptyList())
        return repository.findSimilar(query.trim())
    }
}
