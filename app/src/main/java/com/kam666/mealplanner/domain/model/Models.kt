package com.kam666.mealplanner.domain.model

import java.time.LocalDate

data class Recipe(
    val id: Long = 0,
    val name: String,
    val category: RecipeCategory,
    val servings: Int,
    val imageUri: String? = null,
    val preparationTimeMinutes: Int? = null,
    val preparationSteps: List<String> = emptyList(),
    val ingredients: List<RecipeIngredient> = emptyList()
)

data class Ingredient(
    val id: Long = 0,
    val name: String,
    val unit: IngredientUnit,
    val supermarketCategory: String? = null
)

data class RecipeIngredient(
    val recipeId: Long = 0,
    val ingredientId: Long,
    val ingredient: Ingredient,
    val quantity: Double
)

data class MealPlan(
    val id: Long = 0,
    val date: LocalDate,
    val recipeId: Long,
    val recipe: Recipe,
    val peopleCount: Int,
    val mealType: MealType
)

data class ShoppingListItem(
    val ingredient: Ingredient,
    val totalQuantity: Double
)
