package com.kam666.mealplanner.domain.model

data class RecipeSuggestion(
    val title: String,
    val description: String,
    val category: RecipeCategory,
    val estimatedTimeMinutes: Int?,
    val suggestedIngredients: List<SuggestedIngredient>,
    val preparationSteps: List<String>
)

data class SuggestedIngredient(
    val name: String,
    val quantity: Double,
    val unit: String
)
