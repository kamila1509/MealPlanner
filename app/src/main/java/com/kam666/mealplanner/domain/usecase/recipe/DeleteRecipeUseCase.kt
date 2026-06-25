package com.kam666.mealplanner.domain.usecase.recipe

import com.kam666.mealplanner.domain.repository.RecipeRepository
import javax.inject.Inject

class DeleteRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}
