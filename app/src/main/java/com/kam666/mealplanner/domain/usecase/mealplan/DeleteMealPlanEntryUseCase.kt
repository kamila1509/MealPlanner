package com.kam666.mealplanner.domain.usecase.mealplan

import com.kam666.mealplanner.domain.model.MealType
import com.kam666.mealplanner.domain.repository.MealPlanRepository
import java.time.LocalDate
import javax.inject.Inject

class DeleteMealPlanEntryUseCase @Inject constructor(
    private val repository: MealPlanRepository
) {
    suspend operator fun invoke(date: LocalDate, mealType: MealType) =
        repository.delete(date, mealType)
}
