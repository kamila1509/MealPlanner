package com.kam666.mealplanner.domain.usecase.mealplan

import com.kam666.mealplanner.domain.model.MealPlan
import com.kam666.mealplanner.domain.repository.MealPlanRepository
import javax.inject.Inject

class SetMealPlanEntryUseCase @Inject constructor(
    private val repository: MealPlanRepository
) {
    suspend operator fun invoke(entry: MealPlan) = repository.set(entry)
}
