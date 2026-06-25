package com.kam666.mealplanner.presentation.ingredient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kam666.mealplanner.domain.model.Ingredient
import com.kam666.mealplanner.domain.repository.IngredientRepository
import com.kam666.mealplanner.domain.usecase.ingredient.DeleteIngredientUseCase
import com.kam666.mealplanner.domain.usecase.ingredient.GetAllIngredientsUseCase
import com.kam666.mealplanner.domain.usecase.ingredient.SaveIngredientUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IngredientListViewModel @Inject constructor(
    getAllIngredients: GetAllIngredientsUseCase,
    private val saveIngredient: SaveIngredientUseCase,
    private val deleteIngredient: DeleteIngredientUseCase,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    val ingredients: StateFlow<List<Ingredient>> = getAllIngredients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _inUseWarning = MutableStateFlow<Ingredient?>(null)
    val inUseWarning: StateFlow<Ingredient?> = _inUseWarning.asStateFlow()

    fun save(ingredient: Ingredient) {
        viewModelScope.launch { saveIngredient(ingredient) }
    }

    fun tryDelete(ingredient: Ingredient) {
        viewModelScope.launch {
            if (ingredientRepository.isUsedInRecipes(ingredient.id)) {
                _inUseWarning.value = ingredient
            } else {
                deleteIngredient(ingredient.id)
            }
        }
    }

    fun clearInUseWarning() {
        _inUseWarning.value = null
    }
}
