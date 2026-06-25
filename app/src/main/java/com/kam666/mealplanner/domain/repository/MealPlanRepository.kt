package com.kam666.mealplanner.domain.repository

import com.kam666.mealplanner.domain.model.MealPlan
import com.kam666.mealplanner.domain.model.MealType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MealPlanRepository {
    fun getForWeek(weekStart: LocalDate): Flow<List<MealPlan>>
    suspend fun set(entry: MealPlan)
    suspend fun delete(date: LocalDate, mealType: MealType)
}
