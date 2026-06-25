package com.kam666.mealplanner.data.repository

import com.kam666.mealplanner.data.local.dao.IngredientDao
import com.kam666.mealplanner.data.local.dao.MealPlanDao
import com.kam666.mealplanner.data.local.dao.RecipeDao
import com.kam666.mealplanner.data.local.dao.RecipeIngredientDao
import com.kam666.mealplanner.data.mapper.toDomain
import com.kam666.mealplanner.data.mapper.toEntity
import com.kam666.mealplanner.domain.model.MealPlan
import com.kam666.mealplanner.domain.model.MealType
import com.kam666.mealplanner.domain.repository.MealPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class MealPlanRepositoryImpl @Inject constructor(
    private val mealPlanDao: MealPlanDao,
    private val recipeDao: RecipeDao,
    private val recipeIngredientDao: RecipeIngredientDao,
    private val ingredientDao: IngredientDao
) : MealPlanRepository {

    override fun getForWeek(weekStart: LocalDate): Flow<List<MealPlan>> {
        val startEpoch = weekStart.toEpochDay()
        val endEpoch = weekStart.plusDays(6).toEpochDay()
        return mealPlanDao.getForDateRange(startEpoch, endEpoch).map { entities ->
            entities.mapNotNull { entity ->
                val recipeEntity = recipeDao.getByIdOnce(entity.recipeId) ?: return@mapNotNull null
                val riEntities = recipeIngredientDao.getForRecipeOnce(entity.recipeId)
                val ingredients = riEntities.mapNotNull { ri ->
                    ingredientDao.getByIdOnce(ri.ingredientId)?.let { ing ->
                        ri.toDomain(ing.toDomain())
                    }
                }
                val recipe = recipeEntity.toDomain(ingredients)
                entity.toDomain(recipe)
            }
        }
    }

    override suspend fun set(entry: MealPlan) {
        mealPlanDao.insert(entry.toEntity())
    }

    override suspend fun delete(date: LocalDate, mealType: MealType) {
        mealPlanDao.deleteByDateAndType(date.toEpochDay(), mealType.name)
    }
}
