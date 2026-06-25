package com.kam666.mealplanner.data.repository

import com.kam666.mealplanner.data.local.dao.IngredientDao
import com.kam666.mealplanner.data.local.dao.RecipeIngredientDao
import com.kam666.mealplanner.data.mapper.toDomain
import com.kam666.mealplanner.data.mapper.toEntity
import com.kam666.mealplanner.domain.model.Ingredient
import com.kam666.mealplanner.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IngredientRepositoryImpl @Inject constructor(
    private val dao: IngredientDao,
    private val recipeIngredientDao: RecipeIngredientDao
) : IngredientRepository {

    override fun getAll(): Flow<List<Ingredient>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun search(query: String): Flow<List<Ingredient>> =
        dao.search(query).map { list -> list.map { it.toDomain() } }

    override fun findSimilar(query: String): Flow<List<Ingredient>> =
        dao.findSimilar(query).map { list -> list.map { it.toDomain() } }

    override suspend fun isUsedInRecipes(id: Long): Boolean =
        recipeIngredientDao.countForIngredient(id) > 0

    override suspend fun save(ingredient: Ingredient): Long {
        val entity = ingredient.toEntity()
        return if (entity.id > 0L) {
            dao.update(entity)
            entity.id
        } else {
            dao.insert(entity)
        }
    }

    override suspend fun delete(id: Long) =
        dao.deleteById(id)
}
