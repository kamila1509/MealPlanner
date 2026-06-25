package com.kam666.mealplanner.domain.usecase.ingredient

import com.kam666.mealplanner.domain.model.Ingredient
import com.kam666.mealplanner.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchIngredientsUseCase @Inject constructor(
    private val repository: IngredientRepository
) {
    operator fun invoke(query: String): Flow<List<Ingredient>> = repository.search(query)
}
