package com.kam666.mealplanner.domain.usecase.ai

import com.kam666.mealplanner.domain.model.Ingredient
import com.kam666.mealplanner.domain.model.RecipeSuggestion
import com.kam666.mealplanner.domain.repository.AiRepository
import javax.inject.Inject

class SuggestRecipesUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(
        availableIngredients: List<Ingredient>,
        userPreferences: String,
        excludeTitles: List<String> = emptyList()
    ): List<RecipeSuggestion> {
        val ingredientNames = availableIngredients.map { it.name }
        return aiRepository.suggestRecipes(ingredientNames, userPreferences, excludeTitles)
    }
}
