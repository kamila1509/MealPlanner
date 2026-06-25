package com.kam666.mealplanner.presentation.recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kam666.mealplanner.domain.model.Ingredient
import com.kam666.mealplanner.domain.model.IngredientUnit
import com.kam666.mealplanner.domain.model.Recipe
import com.kam666.mealplanner.domain.model.RecipeCategory
import com.kam666.mealplanner.domain.model.RecipeIngredient
import com.kam666.mealplanner.domain.usecase.ingredient.FindSimilarIngredientsUseCase
import com.kam666.mealplanner.domain.usecase.ingredient.SaveIngredientUseCase
import com.kam666.mealplanner.domain.usecase.recipe.GetRecipeByIdUseCase
import com.kam666.mealplanner.domain.usecase.recipe.SaveRecipeUseCase
import com.kam666.mealplanner.presentation.common.AppPreferencesHolder
import com.kam666.mealplanner.presentation.common.RecipeVisuals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IngredientDraft(val ingredient: Ingredient, val quantity: Double)

data class RecipeEditUiState(
    val name: String = "",
    val category: RecipeCategory = RecipeCategory.ALMUERZO,
    val selectedEmoji: String = RecipeVisuals.pickerEmojis.first(),
    val servings: Int = 2, // overridden in init with prefs value for new recipes
    val preparationTimeMinutes: Int? = 30,
    val preparationText: String = "",
    val ingredients: List<IngredientDraft> = emptyList(),
    val ingredientSuggestions: Map<Long, List<Ingredient>> = emptyMap(),
    val exactMatchWarnings: Map<Long, Ingredient?> = emptyMap(),
    val isSaving: Boolean = false,
    val savedEvent: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRecipeById: GetRecipeByIdUseCase,
    private val saveRecipe: SaveRecipeUseCase,
    private val saveIngredient: SaveIngredientUseCase,
    private val findSimilarIngredients: FindSimilarIngredientsUseCase,
    prefs: AppPreferencesHolder
) : ViewModel() {

    private val recipeId: Long = savedStateHandle.get<Long>("recipeId") ?: 0L
    val isNewRecipe: Boolean = recipeId == 0L

    private val _uiState = MutableStateFlow(
        RecipeEditUiState(isLoading = recipeId != 0L, servings = if (recipeId == 0L) prefs.defaultServings.value else 2)
    )
    val uiState: StateFlow<RecipeEditUiState> = _uiState.asStateFlow()

    private var nextLocalIngredientId = -1L
    private val searchJobs = mutableMapOf<Long, Job>()

    init {
        if (recipeId != 0L) {
            viewModelScope.launch {
                getRecipeById(recipeId).firstOrNull()?.let { recipe ->
                    _uiState.update { state ->
                        state.copy(
                            name = recipe.name,
                            category = recipe.category,
                            selectedEmoji = recipe.imageUri?.takeIf { it.isNotBlank() }
                                ?: RecipeVisuals.emoji(recipe),
                            servings = recipe.servings,
                            preparationTimeMinutes = recipe.preparationTimeMinutes,
                            preparationText = recipe.preparationSteps.joinToString("\n"),
                            ingredients = recipe.ingredients.map { IngredientDraft(it.ingredient, it.quantity) },
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }
    fun onCategoryChange(category: RecipeCategory) = _uiState.update { it.copy(category = category) }
    fun onEmojiChange(emoji: String) = _uiState.update { it.copy(selectedEmoji = emoji) }
    fun onServingsChange(servings: Int) = _uiState.update { it.copy(servings = servings.coerceAtLeast(1)) }
    fun onPrepTimeChange(minutes: Int?) = _uiState.update { it.copy(preparationTimeMinutes = minutes) }
    fun onPreparationTextChange(text: String) = _uiState.update { it.copy(preparationText = text) }

    fun onAddEmptyIngredient() {
        val ingredient = Ingredient(
            id = nextLocalIngredientId--,
            name = "",
            unit = IngredientUnit.G,
            supermarketCategory = SupermarketDefaults.VEG
        )
        _uiState.update { it.copy(ingredients = it.ingredients + IngredientDraft(ingredient, 0.0)) }
    }

    fun onUpdateIngredient(ingredientId: Long, transform: (IngredientDraft) -> IngredientDraft) {
        _uiState.update { state ->
            state.copy(
                ingredients = state.ingredients.map { draft ->
                    if (draft.ingredient.id == ingredientId) transform(draft) else draft
                }
            )
        }
    }

    fun onIngredientNameChanged(ingredientId: Long, name: String) {
        onUpdateIngredient(ingredientId) { d -> d.copy(ingredient = d.ingredient.copy(name = name)) }
        searchJobs[ingredientId]?.cancel()
        searchJobs[ingredientId] = viewModelScope.launch {
            delay(300L)
            findSimilarIngredients(name).collect { suggestions ->
                val typedLower = name.trim().lowercase()
                val exactMatch = suggestions.firstOrNull { it.name.trim().lowercase() == typedLower }
                val similar = suggestions.filter { it.name.trim().lowercase() != typedLower }
                _uiState.update { state ->
                    state.copy(
                        ingredientSuggestions = state.ingredientSuggestions + (ingredientId to similar),
                        exactMatchWarnings = state.exactMatchWarnings + (ingredientId to exactMatch)
                    )
                }
            }
        }
    }

    fun onSuggestionSelected(draftId: Long, existing: Ingredient) {
        searchJobs[draftId]?.cancel()
        searchJobs.remove(draftId)
        _uiState.update { state ->
            state.copy(
                ingredients = state.ingredients.map { draft ->
                    if (draft.ingredient.id == draftId) draft.copy(ingredient = existing) else draft
                },
                ingredientSuggestions = state.ingredientSuggestions - draftId,
                exactMatchWarnings = state.exactMatchWarnings - draftId
            )
        }
    }

    fun onDismissSuggestions(draftId: Long) {
        _uiState.update { it.copy(
            ingredientSuggestions = it.ingredientSuggestions - draftId,
            exactMatchWarnings = it.exactMatchWarnings - draftId
        ) }
    }

    fun onRemoveIngredient(ingredientId: Long) = _uiState.update { state ->
        state.copy(ingredients = state.ingredients.filter { it.ingredient.id != ingredientId })
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank() || state.isSaving || state.savedEvent) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val recipeIngredients = state.ingredients
                .filter { it.ingredient.name.isNotBlank() && it.quantity > 0 }
                .map { draft ->
                    val ingredient = persistIngredient(draft.ingredient)
                    RecipeIngredient(
                        recipeId = recipeId,
                        ingredientId = ingredient.id,
                        ingredient = ingredient,
                        quantity = draft.quantity
                    )
                }

            val recipe = Recipe(
                id = recipeId,
                name = state.name.trim(),
                category = state.category,
                servings = state.servings,
                imageUri = state.selectedEmoji,
                preparationTimeMinutes = state.preparationTimeMinutes,
                preparationSteps = state.preparationText
                    .lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() },
                ingredients = recipeIngredients
            )
            saveRecipe(recipe)
            _uiState.update { it.copy(savedEvent = true) }
        }
    }

    private suspend fun persistIngredient(ingredient: Ingredient): Ingredient {
        if (ingredient.id > 0) return ingredient  // already in DB, skip INSERT OR REPLACE
        val id = saveIngredient(ingredient.copy(id = 0))
        return ingredient.copy(id = id)
    }

    private object SupermarketDefaults {
        const val VEG = "veg"
    }
}
