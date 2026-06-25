package com.kam666.mealplanner.domain.repository

import com.kam666.mealplanner.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun getAll(): Flow<List<Ingredient>>
    fun search(query: String): Flow<List<Ingredient>>
    fun findSimilar(query: String): Flow<List<Ingredient>>
    suspend fun isUsedInRecipes(id: Long): Boolean
    suspend fun save(ingredient: Ingredient): Long
    suspend fun delete(id: Long)
}
