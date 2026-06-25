package com.kam666.mealplanner.domain.usecase.ingredient

import com.kam666.mealplanner.domain.model.Ingredient
import com.kam666.mealplanner.domain.repository.IngredientRepository
import javax.inject.Inject

class SaveIngredientUseCase @Inject constructor(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(ingredient: Ingredient): Long = repository.save(ingredient)
}
