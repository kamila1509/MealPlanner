package com.kam666.mealplanner.presentation.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kam666.mealplanner.domain.model.ShoppingListItem
import com.kam666.mealplanner.domain.usecase.mealplan.GetWeekMealPlanUseCase
import com.kam666.mealplanner.domain.usecase.shoppinglist.GenerateShoppingListUseCase
import com.kam666.mealplanner.presentation.common.SupermarketCategories
import com.kam666.mealplanner.presentation.common.WeekSelectionHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ShoppingGroup(
    val key: String,
    val emoji: String,
    val labelRes: Int,
    val items: List<ShoppingListItem>
)

data class ShoppingListUiState(
    val groups: List<ShoppingGroup> = emptyList(),
    val itemCount: Int = 0,
    val recipeCount: Int = 0,
    val mealCount: Int = 0
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val weekSelection: WeekSelectionHolder,
    private val generateShoppingList: GenerateShoppingListUseCase,
    private val getWeekMealPlan: GetWeekMealPlanUseCase
) : ViewModel() {

    val weekStart: StateFlow<LocalDate> = weekSelection.weekStart

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ShoppingListUiState> = weekSelection.weekStart.flatMapLatest { start ->
        combine(
            generateShoppingList(start),
            getWeekMealPlan(start)
        ) { items, mealPlans ->
            val groups = SupermarketCategories.all.mapNotNull { cat ->
                val groupItems = items.filter {
                    SupermarketCategories.forKey(it.ingredient.supermarketCategory).key == cat.key
                }
                if (groupItems.isEmpty()) null else ShoppingGroup(cat.key, cat.emoji, cat.labelRes, groupItems)
            }
            ShoppingListUiState(
                groups = groups,
                itemCount = items.size,
                recipeCount = mealPlans.map { it.recipeId }.distinct().size,
                mealCount = mealPlans.size
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000, replayExpirationMillis = Long.MAX_VALUE),
        ShoppingListUiState()
    )

    val checkedItems: MutableStateFlow<Set<Long>> = MutableStateFlow(emptySet())

    init {
        viewModelScope.launch {
            weekSelection.weekStart.drop(1).collect {
                checkedItems.value = emptySet()
            }
        }
    }

    fun prevWeek() = weekSelection.prevWeek()
    fun nextWeek() = weekSelection.nextWeek()

    fun toggleItem(ingredientId: Long) {
        checkedItems.update { current ->
            if (ingredientId in current) current - ingredientId else current + ingredientId
        }
    }

    fun weekLabel(): String {
        val start = weekSelection.weekStart.value
        val end = start.plusDays(6)
        val fmt = DateTimeFormatter.ofPattern("d MMM")
        return "${start.format(fmt)} – ${end.format(fmt)}"
    }

    fun progressPercent(checked: Set<Long>): Int {
        val total = uiState.value.itemCount
        if (total == 0) return 0
        return (checked.size * 100) / total
    }
}
