package com.kam666.mealplanner.domain.repository

import com.kam666.mealplanner.domain.model.RecipeSuggestion

interface AiRepository {
    suspend fun suggestRecipes(
        availableIngredients: List<String>,
        userPreferences: String,
        excludeTitles: List<String> = emptyList()
    ): List<RecipeSuggestion>

    suspend fun extractRecipeFromImage(imageBase64: String, mimeType: String): RecipeSuggestion?

    suspend fun extractRecipeFromText(rawText: String): RecipeSuggestion?
}
