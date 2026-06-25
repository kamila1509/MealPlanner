package com.kam666.mealplanner.domain.usecase.recipe

import com.kam666.mealplanner.domain.model.Recipe
import com.kam666.mealplanner.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecipeByIdUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    operator fun invoke(id: Long): Flow<Recipe?> = repository.getById(id)
}
