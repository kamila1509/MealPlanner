package com.kam666.mealplanner.presentation.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeekSelectionHolder @Inject constructor() {
    val weekStart: MutableStateFlow<LocalDate> =
        MutableStateFlow(LocalDate.now().with(DayOfWeek.MONDAY))

    fun prevWeek() = weekStart.update { it.minusWeeks(1) }
    fun nextWeek() = weekStart.update { it.plusWeeks(1) }
}
