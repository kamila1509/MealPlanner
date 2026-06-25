package com.kam666.mealplanner.presentation.aisuggestions

import com.kam666.mealplanner.domain.model.RecipeSuggestion

sealed class AiSuggestionsUiState {
    object Idle : AiSuggestionsUiState()
    object Loading : AiSuggestionsUiState()
    data class Success(val suggestions: List<RecipeSuggestion>) : AiSuggestionsUiState()
    data class Error(val message: String) : AiSuggestionsUiState()
}
