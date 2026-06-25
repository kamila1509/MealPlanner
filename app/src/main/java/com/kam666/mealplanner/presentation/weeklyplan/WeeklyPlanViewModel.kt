package com.kam666.mealplanner.presentation.weeklyplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kam666.mealplanner.domain.model.MealPlan
import com.kam666.mealplanner.domain.model.MealType
import com.kam666.mealplanner.domain.model.Recipe
import com.kam666.mealplanner.domain.usecase.mealplan.DeleteMealPlanEntryUseCase
import com.kam666.mealplanner.domain.usecase.mealplan.GetWeekMealPlanUseCase
import com.kam666.mealplanner.domain.usecase.mealplan.SetMealPlanEntryUseCase
import com.kam666.mealplanner.domain.usecase.recipe.GetAllRecipesUseCase
import com.kam666.mealplanner.presentation.common.PendingMealPlanHolder
import com.kam666.mealplanner.presentation.common.WeekSelectionHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class MealSlotUiModel(val mealPlanId: Long, val recipe: Recipe, val peopleCount: Int)

data class DayPlanUiModel(
    val date: LocalDate,
    val almuerzo: MealSlotUiModel?,
    val cena: MealSlotUiModel?
)

data class WeeklyPlanUiState(
    val weekStart: LocalDate,
    val days: List<DayPlanUiModel> = emptyList()
)

@HiltViewModel
class WeeklyPlanViewModel @Inject constructor(
    private val weekSelection: WeekSelectionHolder,
    private val pendingMealPlanHolder: PendingMealPlanHolder,
    private val getWeekMealPlan: GetWeekMealPlanUseCase,
    private val setMealPlanEntry: SetMealPlanEntryUseCase,
    private val deleteMealPlanEntry: DeleteMealPlanEntryUseCase,
    getAllRecipes: GetAllRecipesUseCase
) : ViewModel() {

    val weekStart: StateFlow<LocalDate> = weekSelection.weekStart
    val pendingRecipeId: StateFlow<Long?> = pendingMealPlanHolder.recipeId

    val allRecipes: StateFlow<List<Recipe>> = getAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<WeeklyPlanUiState> = weekStart.flatMapLatest { start ->
        getWeekMealPlan(start).map { mealPlans ->
            val days = (0L..6L).map { offset ->
                val date = start.plusDays(offset)
                val dayPlans = mealPlans.filter { it.date == date }
                DayPlanUiModel(
                    date = date,
                    almuerzo = dayPlans.find { it.mealType == MealType.ALMUERZO }
                        ?.let { MealSlotUiModel(it.id, it.recipe, it.peopleCount) },
                    cena = dayPlans.find { it.mealType == MealType.CENA }
                        ?.let { MealSlotUiModel(it.id, it.recipe, it.peopleCount) }
                )
            }
            WeeklyPlanUiState(weekStart = start, days = days)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000, replayExpirationMillis = Long.MAX_VALUE),
        WeeklyPlanUiState(weekStart = weekStart.value)
    )

    val mealCount: StateFlow<Int> = uiState.map { state ->
        state.days.sumOf { day ->
            (if (day.almuerzo != null) 1 else 0) + (if (day.cena != null) 1 else 0)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun cancelPending() = pendingMealPlanHolder.clear()

    fun prevWeek() = weekSelection.prevWeek()
    fun nextWeek() = weekSelection.nextWeek()

    fun setEntry(date: LocalDate, mealType: MealType, recipe: Recipe, peopleCount: Int) {
        viewModelScope.launch {
            setMealPlanEntry(
                MealPlan(
                    date = date,
                    recipeId = recipe.id,
                    recipe = recipe,
                    peopleCount = peopleCount,
                    mealType = mealType
                )
            )
            pendingMealPlanHolder.clear()
        }
    }

    fun deleteEntry(date: LocalDate, mealType: MealType) {
        viewModelScope.launch { deleteMealPlanEntry(date, mealType) }
    }

    fun randomFillWeek() {
        val recipes = allRecipes.value
        if (recipes.isEmpty()) return
        val state = uiState.value
        viewModelScope.launch {
            state.days.forEach { day ->
                if (day.almuerzo == null) {
                    val recipe = recipes.random()
                    setMealPlanEntry(
                        MealPlan(
                            date = day.date,
                            recipeId = recipe.id,
                            recipe = recipe,
                            peopleCount = recipe.servings,
                            mealType = MealType.ALMUERZO
                        )
                    )
                }
                if (day.cena == null) {
                    val recipe = recipes.random()
                    setMealPlanEntry(
                        MealPlan(
                            date = day.date,
                            recipeId = recipe.id,
                            recipe = recipe,
                            peopleCount = recipe.servings,
                            mealType = MealType.CENA
                        )
                    )
                }
            }
        }
    }

    fun weekLabel(): String {
        val start = weekSelection.weekStart.value
        val end = start.plusDays(6)
        val fmt = DateTimeFormatter.ofPattern("d MMM")
        return "${start.format(fmt)} – ${end.format(fmt)}"
    }
}
