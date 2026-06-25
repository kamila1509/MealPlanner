package com.kam666.mealplanner.presentation.aisuggestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kam666.mealplanner.domain.model.RecipeSuggestion
import com.kam666.mealplanner.domain.usecase.ai.SaveSuggestedRecipeUseCase
import com.kam666.mealplanner.domain.usecase.ai.SuggestRecipesUseCase
import com.kam666.mealplanner.domain.usecase.ingredient.GetAllIngredientsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiSuggestionsViewModel @Inject constructor(
    private val suggestRecipesUseCase: SuggestRecipesUseCase,
    private val saveSuggestedRecipeUseCase: SaveSuggestedRecipeUseCase,
    private val getAllIngredientsUseCase: GetAllIngredientsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiSuggestionsUiState>(AiSuggestionsUiState.Idle)
    val uiState: StateFlow<AiSuggestionsUiState> = _uiState.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _savedMessage = MutableStateFlow<String?>(null)
    val savedMessage: StateFlow<String?> = _savedMessage.asStateFlow()

    private var lastPreferences: String = ""

    fun suggest(userPreferences: String) {
        lastPreferences = userPreferences
        viewModelScope.launch {
            _uiState.value = AiSuggestionsUiState.Loading
            runCatching {
                val ingredients = getAllIngredientsUseCase().first()
                suggestRecipesUseCase(ingredients, userPreferences)
            }.fold(
                onSuccess = { suggestions ->
                    _uiState.value = if (suggestions.isEmpty())
                        AiSuggestionsUiState.Error("No se recibieron sugerencias. Intenta de nuevo.")
                    else
                        AiSuggestionsUiState.Success(suggestions)
                },
                onFailure = { e ->
                    _uiState.value = AiSuggestionsUiState.Error(e.message ?: "Error desconocido")
                }
            )
        }
    }

    fun loadMore() {
        val current = _uiState.value as? AiSuggestionsUiState.Success ?: return
        viewModelScope.launch {
            _isLoadingMore.value = true
            val existingTitles = current.suggestions.map { it.title }
            runCatching {
                val ingredients = getAllIngredientsUseCase().first()
                suggestRecipesUseCase(ingredients, lastPreferences, existingTitles)
            }.fold(
                onSuccess = { newSuggestions ->
                    if (newSuggestions.isNotEmpty()) {
                        _uiState.value = current.copy(
                            suggestions = current.suggestions + newSuggestions
                        )
                    }
                },
                onFailure = { /* mantiene las actuales, no rompe la UI */ }
            )
            _isLoadingMore.value = false
        }
    }

    fun saveRecipe(suggestion: RecipeSuggestion) {
        viewModelScope.launch {
            runCatching { saveSuggestedRecipeUseCase(suggestion) }
                .fold(
                    onSuccess = { _savedMessage.value = "\"${suggestion.title}\" guardada en tus recetas" },
                    onFailure = { _savedMessage.value = "Error al guardar la receta" }
                )
        }
    }

    fun clearSavedMessage() { _savedMessage.value = null }
}
