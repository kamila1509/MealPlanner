package com.kam666.mealplanner.presentation.common

import androidx.compose.ui.graphics.Color
import com.kam666.mealplanner.domain.model.Recipe
import com.kam666.mealplanner.domain.model.RecipeCategory
import com.kam666.mealplanner.presentation.theme.RecipeTints

object RecipeVisuals {
    private val categoryEmojis = mapOf(
        RecipeCategory.DESAYUNO to "🍳",
        RecipeCategory.ALMUERZO to "🍽️",
        RecipeCategory.CENA to "🌙",
        RecipeCategory.SNACK to "🥪",
        RecipeCategory.POSTRE to "🍰"
    )

    private val defaultEmojis = listOf("🍲", "🥩", "🥔", "🍗", "🍚", "🌶️", "🍝", "🥞", "🍓", "🥗", "🐟", "🦐")

    val pickerEmojis: List<String> = listOf("🍲", "🥩", "🥔", "🍗", "🍚", "🌶️", "🐟", "🦐")

    fun emoji(recipe: Recipe): String {
        val stored = recipe.imageUri?.trim().orEmpty()
        if (stored.isNotEmpty()) return stored
        return categoryEmojis[recipe.category] ?: defaultEmojis[(recipe.id % defaultEmojis.size).toInt()]
    }

    fun tint(recipe: Recipe): Color {
        val index = ((recipe.id * 31) + recipe.name.hashCode()).let { if (it < 0) -it else it }
        return RecipeTints[(index % RecipeTints.size).toInt()]
    }

    fun categoryEmoji(category: RecipeCategory): String =
        categoryEmojis[category] ?: "🍲"
}
