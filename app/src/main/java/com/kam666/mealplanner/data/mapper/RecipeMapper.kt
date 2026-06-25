package com.kam666.mealplanner.data.mapper

import com.kam666.mealplanner.data.local.entity.RecipeEntity
import com.kam666.mealplanner.data.local.entity.RecipeIngredientEntity
import com.kam666.mealplanner.domain.model.Ingredient
import com.kam666.mealplanner.domain.model.Recipe
import com.kam666.mealplanner.domain.model.RecipeCategory
import com.kam666.mealplanner.domain.model.RecipeIngredient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun RecipeEntity.toDomain(ingredients: List<RecipeIngredient> = emptyList()) = Recipe(
    id = id,
    name = name,
    category = RecipeCategory.valueOf(category),
    servings = servings,
    imageUri = imageUri,
    preparationTimeMinutes = preparationTimeMinutes,
    preparationSteps = Json.decodeFromString(preparationSteps),
    ingredients = ingredients
)

fun Recipe.toEntity() = RecipeEntity(
    id = id,
    name = name,
    category = category.name,
    servings = servings,
    imageUri = imageUri,
    preparationTimeMinutes = preparationTimeMinutes,
    preparationSteps = Json.encodeToString(preparationSteps)
)

fun RecipeIngredientEntity.toDomain(ingredient: Ingredient) = RecipeIngredient(
    recipeId = recipeId,
    ingredientId = ingredientId,
    ingredient = ingredient,
    quantity = quantity
)

fun RecipeIngredient.toEntity() = RecipeIngredientEntity(
    recipeId = recipeId,
    ingredientId = ingredientId,
    quantity = quantity
)
