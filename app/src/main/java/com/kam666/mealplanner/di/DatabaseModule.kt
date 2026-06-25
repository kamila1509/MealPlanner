package com.kam666.mealplanner.di

import android.content.Context
import androidx.room.Room
import com.kam666.mealplanner.data.local.MealPlannerDatabase
import com.kam666.mealplanner.data.local.dao.IngredientDao
import com.kam666.mealplanner.data.local.dao.MealPlanDao
import com.kam666.mealplanner.data.local.dao.RecipeDao
import com.kam666.mealplanner.data.local.dao.RecipeIngredientDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MealPlannerDatabase =
        Room.databaseBuilder(context, MealPlannerDatabase::class.java, "meal_planner.db")
            .build()

    @Provides
    fun provideRecipeDao(db: MealPlannerDatabase): RecipeDao = db.recipeDao()

    @Provides
    fun provideIngredientDao(db: MealPlannerDatabase): IngredientDao = db.ingredientDao()

    @Provides
    fun provideRecipeIngredientDao(db: MealPlannerDatabase): RecipeIngredientDao = db.recipeIngredientDao()

    @Provides
    fun provideMealPlanDao(db: MealPlannerDatabase): MealPlanDao = db.mealPlanDao()
}
