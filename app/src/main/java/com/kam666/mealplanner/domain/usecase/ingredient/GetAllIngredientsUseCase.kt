package com.kam666.mealplanner.domain.usecase.ingredient

import com.kam666.mealplanner.domain.model.Ingredient
import com.kam666.mealplanner.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllIngredientsUseCase @Inject constructor(
    private val repository: IngredientRepository
) {
    operator fun invoke(): Flow<List<Ingredient>> = repository.getAll()
}
