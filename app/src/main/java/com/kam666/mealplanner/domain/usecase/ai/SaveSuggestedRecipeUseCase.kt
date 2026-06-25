package com.kam666.mealplanner.domain.usecase.ai

import com.kam666.mealplanner.domain.model.Ingredient
import com.kam666.mealplanner.domain.model.IngredientUnit
import com.kam666.mealplanner.domain.model.Recipe
import com.kam666.mealplanner.domain.model.RecipeIngredient
import com.kam666.mealplanner.domain.model.RecipeSuggestion
import com.kam666.mealplanner.domain.usecase.ingredient.SaveIngredientUseCase
import com.kam666.mealplanner.domain.usecase.recipe.SaveRecipeUseCase
import javax.inject.Inject

class SaveSuggestedRecipeUseCase @Inject constructor(
    private val saveRecipeUseCase: SaveRecipeUseCase,
    private val saveIngredientUseCase: SaveIngredientUseCase
) {
    suspend operator fun invoke(suggestion: RecipeSuggestion): Long {
        // Cada ingrediente sugerido debe existir en la BD antes de vincularlo a la receta
        val recipeIngredients = suggestion.suggestedIngredients.map { si ->
            val unit = IngredientUnit.entries.firstOrNull {
                it.name.equals(si.unit, ignoreCase = true)
            } ?: IngredientUnit.UNIDAD
            val ingredient = Ingredient(name = si.name, unit = unit)
            val ingredientId = saveIngredientUseCase(ingredient)
            RecipeIngredient(
                ingredientId = ingredientId,
                ingredient = ingredient.copy(id = ingredientId),
                quantity = si.quantity
            )
        }
        val recipe = Recipe(
            name = suggestion.title,
            category = suggestion.category,
            servings = 2,
            preparationTimeMinutes = suggestion.estimatedTimeMinutes,
            preparationSteps = suggestion.preparationSteps,
            ingredients = recipeIngredients
        )
        return saveRecipeUseCase(recipe)
    }
}
