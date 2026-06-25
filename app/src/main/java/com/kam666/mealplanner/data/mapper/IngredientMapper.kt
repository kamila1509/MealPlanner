package com.kam666.mealplanner.data.mapper

import com.kam666.mealplanner.data.local.entity.IngredientEntity
import com.kam666.mealplanner.domain.model.Ingredient
import com.kam666.mealplanner.domain.model.IngredientUnit

fun IngredientEntity.toDomain() = Ingredient(
    id = id,
    name = name,
    unit = IngredientUnit.valueOf(unit),
    supermarketCategory = supermarketCategory
)

fun Ingredient.toEntity() = IngredientEntity(
    id = id,
    name = name,
    unit = unit.name,
    supermarketCategory = supermarketCategory
)
