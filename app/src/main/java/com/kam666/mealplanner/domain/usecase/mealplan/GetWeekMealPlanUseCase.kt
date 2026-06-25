package com.kam666.mealplanner.domain.usecase.mealplan

import com.kam666.mealplanner.domain.model.MealPlan
import com.kam666.mealplanner.domain.repository.MealPlanRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetWeekMealPlanUseCase @Inject constructor(
    private val repository: MealPlanRepository
) {
    operator fun invoke(weekStart: LocalDate): Flow<List<MealPlan>> =
        repository.getForWeek(weekStart)
}
