package com.kam666.mealplanner.di

import com.kam666.mealplanner.data.repository.IngredientRepositoryImpl
import com.kam666.mealplanner.data.repository.MealPlanRepositoryImpl
import com.kam666.mealplanner.data.repository.MercadonaRepositoryImpl
import com.kam666.mealplanner.data.repository.RecipeRepositoryImpl
import com.kam666.mealplanner.domain.repository.IngredientRepository
import com.kam666.mealplanner.domain.repository.MealPlanRepository
import com.kam666.mealplanner.data.repository.FirebaseAuthRepositoryImpl
import com.kam666.mealplanner.domain.repository.AuthRepository
import com.kam666.mealplanner.domain.repository.MercadonaRepository
import com.kam666.mealplanner.domain.repository.RecipeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindIngredientRepository(impl: IngredientRepositoryImpl): IngredientRepository

    @Binds
    @Singleton
    abstract fun bindMealPlanRepository(impl: MealPlanRepositoryImpl): MealPlanRepository

    @Binds
    @Singleton
    abstract fun bindMercadonaRepository(impl: MercadonaRepositoryImpl): MercadonaRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepositoryImpl): AuthRepository
}
