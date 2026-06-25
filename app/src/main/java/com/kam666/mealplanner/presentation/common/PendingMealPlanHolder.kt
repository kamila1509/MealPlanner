package com.kam666.mealplanner.presentation.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingMealPlanHolder @Inject constructor() {
    private val _recipeId = MutableStateFlow<Long?>(null)
    val recipeId: StateFlow<Long?> = _recipeId.asStateFlow()

    fun set(recipeId: Long) = _recipeId.update { recipeId }
    fun clear() = _recipeId.update { null }
}
