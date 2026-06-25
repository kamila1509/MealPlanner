package com.kam666.mealplanner.domain.usecase.backup

import com.kam666.mealplanner.data.local.MealPlannerDatabase
import com.kam666.mealplanner.data.local.entity.IngredientEntity
import com.kam666.mealplanner.data.local.entity.RecipeEntity
import com.kam666.mealplanner.data.local.entity.RecipeIngredientEntity
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ImportBackupUseCase @Inject constructor(
    private val database: MealPlannerDatabase
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend operator fun invoke(jsonString: String): Int {
        val backup = json.decodeFromString<BackupData>(jsonString)
        database.clearAllTables()
        backup.recipes.forEach { r ->
            database.recipeDao().insert(
                RecipeEntity(r.id, r.name, r.category, r.servings, r.imageUri, r.preparationTimeMinutes, r.preparationSteps)
            )
        }
        backup.ingredients.forEach { i ->
            database.ingredientDao().insert(
                IngredientEntity(i.id, i.name, i.unit, i.supermarketCategory)
            )
        }
        backup.recipeIngredients.forEach { l ->
            database.recipeIngredientDao().insert(
                RecipeIngredientEntity(l.recipeId, l.ingredientId, l.quantity)
            )
        }
        return backup.recipes.size
    }
}
