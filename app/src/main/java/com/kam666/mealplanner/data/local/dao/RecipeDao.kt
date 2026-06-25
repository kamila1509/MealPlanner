package com.kam666.mealplanner.data.local.dao

import androidx.room.*
import com.kam666.mealplanner.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY name ASC")
    fun getAll(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getById(id: Long): Flow<RecipeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecipeEntity): Long

    @Update
    suspend fun update(entity: RecipeEntity)

    @Delete
    suspend fun delete(entity: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getByIdOnce(id: Long): RecipeEntity?

    @Query("SELECT * FROM recipes ORDER BY name ASC")
    suspend fun getAllOnce(): List<RecipeEntity>
}
