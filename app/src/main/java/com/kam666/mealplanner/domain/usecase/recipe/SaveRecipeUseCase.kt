package com.kam666.mealplanner.domain.usecase.recipe

import com.kam666.mealplanner.domain.model.Recipe
import com.kam666.mealplanner.domain.repository.RecipeRepository
import javax.inject.Inject

class SaveRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe): Long =
        repository.save(recipe, recipe.ingredients)
}
