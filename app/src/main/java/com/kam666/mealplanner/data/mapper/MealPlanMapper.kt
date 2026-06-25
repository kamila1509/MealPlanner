package com.kam666.mealplanner.data.mapper

import com.kam666.mealplanner.data.local.entity.MealPlanEntity
import com.kam666.mealplanner.domain.model.MealPlan
import com.kam666.mealplanner.domain.model.MealType
import com.kam666.mealplanner.domain.model.Recipe
import java.time.LocalDate

fun MealPlanEntity.toDomain(recipe: Recipe) = MealPlan(
    id = id,
    date = LocalDate.ofEpochDay(date),
    recipeId = recipeId,
    recipe = recipe,
    peopleCount = peopleCount,
    mealType = MealType.valueOf(mealType)
)

fun MealPlan.toEntity() = MealPlanEntity(
    id = id,
    date = date.toEpochDay(),
    recipeId = recipeId,
    peopleCount = peopleCount,
    mealType = mealType.name
)
