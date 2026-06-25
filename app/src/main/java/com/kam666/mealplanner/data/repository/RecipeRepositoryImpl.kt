package com.kam666.mealplanner.data.repository

import androidx.room.withTransaction
import com.kam666.mealplanner.data.local.MealPlannerDatabase
import com.kam666.mealplanner.data.local.dao.IngredientDao
import com.kam666.mealplanner.data.local.dao.RecipeDao
import com.kam666.mealplanner.data.local.dao.RecipeIngredientDao
import com.kam666.mealplanner.data.mapper.toDomain
import com.kam666.mealplanner.data.mapper.toEntity
import com.kam666.mealplanner.domain.model.Recipe
import com.kam666.mealplanner.domain.model.RecipeIngredient
import com.kam666.mealplanner.domain.repository.RecipeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val database: MealPlannerDatabase,
    private val recipeDao: RecipeDao,
    private val recipeIngredientDao: RecipeIngredientDao,
    private val ingredientDao: IngredientDao
) : RecipeRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAll(): Flow<List<Recipe>> =
        recipeDao.getAll().map { recipes ->
            recipes.map { entity ->
                val riEntities = recipeIngredientDao.getForRecipeOnce(entity.id)
                val ingredients = riEntities.mapNotNull { ri ->
                    ingredientDao.getByIdOnce(ri.ingredientId)?.let { ing ->
                        ri.toDomain(ing.toDomain())
                    }
                }
                entity.toDomain(ingredients)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getById(id: Long): Flow<Recipe?> =
        recipeDao.getById(id).flatMapLatest { entity ->
            if (entity == null) {
                flowOf(null)
            } else {
                recipeIngredientDao.getForRecipe(id).map { riEntities ->
                    val ingredients = riEntities.mapNotNull { ri ->
                        ingredientDao.getByIdOnce(ri.ingredientId)?.let { ing ->
                            ri.toDomain(ing.toDomain())
                        }
                    }
                    entity.toDomain(ingredients)
                }
            }
        }

    override suspend fun save(recipe: Recipe, ingredients: List<RecipeIngredient>): Long =
        database.withTransaction {
            val id = recipeDao.insert(recipe.toEntity())
            recipeIngredientDao.deleteAllForRecipe(id)
            recipeIngredientDao.insertAll(
                ingredients.map { it.copy(recipeId = id).toEntity() }
            )
            id
        }

    override suspend fun delete(id: Long) =
        recipeDao.deleteById(id)
}
