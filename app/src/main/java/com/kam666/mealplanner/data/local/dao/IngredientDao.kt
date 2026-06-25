package com.kam666.mealplanner.data.local.dao

import androidx.room.*
import com.kam666.mealplanner.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAll(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<IngredientEntity>>

    @Query("""
        SELECT * FROM ingredients
        WHERE name LIKE '%' || :query || '%'
           OR :query LIKE '%' || name || '%'
        ORDER BY name ASC
        LIMIT 5
    """)
    fun findSimilar(query: String): Flow<List<IngredientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: IngredientEntity): Long

    @Update
    suspend fun update(entity: IngredientEntity)

    @Delete
    suspend fun delete(entity: IngredientEntity)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getByIdOnce(id: Long): IngredientEntity?

    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    suspend fun getAllOnce(): List<IngredientEntity>
}
