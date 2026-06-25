package com.kam666.mealplanner.domain.usecase.backup

import com.kam666.mealplanner.data.local.dao.IngredientDao
import com.kam666.mealplanner.data.local.dao.RecipeDao
import com.kam666.mealplanner.data.local.dao.RecipeIngredientDao
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportDate: String,
    val recipes: List<RecipeBackup>,
    val ingredients: List<IngredientBackup>,
    val recipeIngredients: List<RecipeIngredientBackup>
)

@Serializable
data class RecipeBackup(
    val id: Long,
    val name: String,
    val category: String,
    val servings: Int,
    val imageUri: String? = null,
    val preparationTimeMinutes: Int? = null,
    val preparationSteps: String = "[]"
)

@Serializable
data class IngredientBackup(
    val id: Long,
    val name: String,
    val unit: String,
    val supermarketCategory: String? = null
)

@Serializable
data class RecipeIngredientBackup(
    val recipeId: Long,
    val ingredientId: Long,
    val quantity: Double
)

class ExportBackupUseCase @Inject constructor(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val recipeIngredientDao: RecipeIngredientDao
) {
    suspend operator fun invoke(): String {
        val recipes = recipeDao.getAllOnce().map { e ->
            RecipeBackup(e.id, e.name, e.category, e.servings, e.imageUri, e.preparationTimeMinutes, e.preparationSteps)
        }
        val ingredients = ingredientDao.getAllOnce().map { e ->
            IngredientBackup(e.id, e.name, e.unit, e.supermarketCategory)
        }
        val links = recipeIngredientDao.getAllOnce().map { e ->
            RecipeIngredientBackup(e.recipeId, e.ingredientId, e.quantity)
        }
        val today = java.time.LocalDate.now().toString()
        val backup = BackupData(recipes = recipes, ingredients = ingredients, recipeIngredients = links, exportDate = today)
        return Json.encodeToString(backup)
    }
}
