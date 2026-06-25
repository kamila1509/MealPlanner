package com.kam666.mealplanner.data.local.dao

import androidx.room.*
import com.kam666.mealplanner.data.local.entity.MealPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plans WHERE date >= :startEpoch AND date <= :endEpoch")
    fun getForDateRange(startEpoch: Long, endEpoch: Long): Flow<List<MealPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MealPlanEntity): Long

    @Delete
    suspend fun delete(entity: MealPlanEntity)

    @Query("DELETE FROM meal_plans WHERE date = :epochDay AND mealType = :mealType")
    suspend fun deleteByDateAndType(epochDay: Long, mealType: String)
}
