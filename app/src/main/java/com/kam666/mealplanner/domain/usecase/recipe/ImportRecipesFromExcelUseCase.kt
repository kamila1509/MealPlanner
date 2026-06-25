package com.kam666.mealplanner.domain.usecase.recipe

import com.kam666.mealplanner.data.local.excel.ExcelImportData
import com.kam666.mealplanner.domain.model.Ingredient
import com.kam666.mealplanner.domain.model.IngredientUnit
import com.kam666.mealplanner.domain.model.Recipe
import com.kam666.mealplanner.domain.model.RecipeCategory
import com.kam666.mealplanner.domain.model.RecipeIngredient
import com.kam666.mealplanner.domain.repository.IngredientRepository
import com.kam666.mealplanner.domain.usecase.ingredient.SaveIngredientUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class ImportResult(val recipesAdded: Int, val ingredientsCreated: Int)

class ImportRecipesFromExcelUseCase @Inject constructor(
    private val saveRecipeUseCase: SaveRecipeUseCase,
    private val saveIngredientUseCase: SaveIngredientUseCase,
    private val ingredientRepository: IngredientRepository
) {
    suspend operator fun invoke(data: ExcelImportData): ImportResult {
        val existingByName = ingredientRepository.getAll().first()
            .associateBy { it.name.trim().lowercase() }
        val ingredientCache = existingByName.entries
            .associate { (name, ing) -> name to ing.id }
            .toMutableMap()
        val ingredientsCreatedBefore = ingredientCache.size

        val ingredientsByRecipe = data.ingredients.groupBy { it.recipeName.trim().lowercase() }
        var recipesAdded = 0

        for (recipeImport in data.recipes) {
            val recipeIngredients = mutableListOf<RecipeIngredient>()

            for (ingImport in ingredientsByRecipe[recipeImport.name.trim().lowercase()].orEmpty()) {
                val key = ingImport.name.trim().lowercase()
                val unit = mapUnit(ingImport.unit)
                val ingId = ingredientCache.getOrPut(key) {
                    saveIngredientUseCase(
                        Ingredient(
                            name = ingImport.name.trim(),
                            unit = unit,
                            supermarketCategory = mapCategory(ingImport.type)
                        )
                    )
                }
                recipeIngredients.add(
                    RecipeIngredient(
                        ingredientId = ingId,
                        ingredient = Ingredient(id = ingId, name = ingImport.name.trim(), unit = unit),
                        quantity = ingImport.quantity.toDouble()
                    )
                )
            }

            saveRecipeUseCase(
                Recipe(
                    name = recipeImport.name.trim(),
                    category = RecipeCategory.ALMUERZO,
                    servings = 2,
                    ingredients = recipeIngredients
                )
            )
            recipesAdded++
        }

        return ImportResult(recipesAdded, ingredientCache.size - ingredientsCreatedBefore)
    }

    private fun mapUnit(raw: String): IngredientUnit = when {
        raw.startsWith("kg", ignoreCase = true)  -> IngredientUnit.KG
        raw.startsWith("ml", ignoreCase = true)  -> IngredientUnit.ML
        raw.startsWith("g", ignoreCase = true)   -> IngredientUnit.G
        raw.startsWith("l", ignoreCase = true)   -> IngredientUnit.L
        raw.contains("cucharadita", ignoreCase = true) || raw.contains("cdta", ignoreCase = true) -> IngredientUnit.CUCHARADITA
        raw.contains("cucharada", ignoreCase = true)   || raw.contains("cda", ignoreCase = true)  -> IngredientUnit.CUCHARADA
        raw.contains("taza", ignoreCase = true)  -> IngredientUnit.TAZA
        raw.contains("sobre", ignoreCase = true) -> IngredientUnit.SOBRE
        else -> IngredientUnit.UNIDAD
    }

    private fun mapCategory(type: String): String = when {
        type.contains("Condimento", ignoreCase = true) || type.contains("Salsa", ignoreCase = true) -> "Condimentos"
        type.contains("Verdura", ignoreCase = true) || type.contains("Fruta", ignoreCase = true) -> "Verduras"
        type.contains("Proteina", ignoreCase = true) || type.contains("Proteína", ignoreCase = true) -> "Carnes"
        type.contains("Legumbre", ignoreCase = true) -> "Despensa"
        type.contains("Grano", ignoreCase = true) || type.contains("Harina", ignoreCase = true) -> "Despensa"
        type.contains("Lacteo", ignoreCase = true) || type.contains("Lácteo", ignoreCase = true) -> "Lácteos"
        type.contains("Bebida", ignoreCase = true) || type.contains("Liquido", ignoreCase = true) -> "Bebidas"
        else -> "Despensa"
    }
}
