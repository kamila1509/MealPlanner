package com.kam666.mealplanner.presentation.recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kam666.mealplanner.domain.model.Recipe
import com.kam666.mealplanner.domain.usecase.recipe.DeleteRecipeUseCase
import com.kam666.mealplanner.domain.usecase.recipe.GetRecipeByIdUseCase
import com.kam666.mealplanner.presentation.common.PendingMealPlanHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getRecipeById: GetRecipeByIdUseCase,
    private val deleteRecipe: DeleteRecipeUseCase,
    private val pendingMealPlanHolder: PendingMealPlanHolder
) : ViewModel() {

    private val recipeId: Long = checkNotNull(savedStateHandle["recipeId"])

    val recipe: StateFlow<Recipe?> = getRecipeById(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete() {
        viewModelScope.launch {
            deleteRecipe(recipeId)
        }
    }

    fun addToPlan(onDone: () -> Unit) {
        pendingMealPlanHolder.set(recipeId)
        onDone()
    }
}
