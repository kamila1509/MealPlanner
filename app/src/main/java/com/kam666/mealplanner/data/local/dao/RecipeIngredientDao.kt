package com.kam666.mealplanner.data.local.dao

import androidx.room.*
import com.kam666.mealplanner.data.local.entity.RecipeIngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientDao {
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    fun getForRecipe(recipeId: Long): Flow<List<RecipeIngredientEntity>>

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun getForRecipeOnce(recipeId: Long): List<RecipeIngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecipeIngredientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<RecipeIngredientEntity>)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteAllForRecipe(recipeId: Long)

    @Query("SELECT COUNT(*) FROM recipe_ingredients WHERE ingredientId = :ingredientId")
    suspend fun countForIngredient(ingredientId: Long): Int

    @Query("SELECT * FROM recipe_ingredients")
    suspend fun getAllOnce(): List<RecipeIngredientEntity>
}
