package com.kam666.mealplanner.domain.usecase.ingredient

import com.kam666.mealplanner.domain.repository.IngredientRepository
import javax.inject.Inject

class DeleteIngredientUseCase @Inject constructor(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}
