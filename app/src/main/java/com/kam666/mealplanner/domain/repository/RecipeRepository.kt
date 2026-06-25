package com.kam666.mealplanner.domain.repository

import com.kam666.mealplanner.domain.model.Recipe
import com.kam666.mealplanner.domain.model.RecipeIngredient
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getAll(): Flow<List<Recipe>>
    fun getById(id: Long): Flow<Recipe?>
    suspend fun save(recipe: Recipe, ingredients: List<RecipeIngredient>): Long
    suspend fun delete(id: Long)
}
