package com.kam666.mealplanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kam666.mealplanner.data.local.converter.DateConverter
import com.kam666.mealplanner.data.local.converter.StringListConverter
import com.kam666.mealplanner.data.local.dao.IngredientDao
import com.kam666.mealplanner.data.local.dao.MealPlanDao
import com.kam666.mealplanner.data.local.dao.RecipeDao
import com.kam666.mealplanner.data.local.dao.RecipeIngredientDao
import com.kam666.mealplanner.data.local.entity.IngredientEntity
import com.kam666.mealplanner.data.local.entity.MealPlanEntity
import com.kam666.mealplanner.data.local.entity.RecipeEntity
import com.kam666.mealplanner.data.local.entity.RecipeIngredientEntity

@Database(
    entities = [RecipeEntity::class, IngredientEntity::class, RecipeIngredientEntity::class, MealPlanEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverter::class, StringListConverter::class)
abstract class MealPlannerDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun mealPlanDao(): MealPlanDao
}
