package com.kam666.mealplanner.presentation.common

import androidx.annotation.StringRes
import com.kam666.mealplanner.R

data class SupermarketCategory(
    val key: String,
    val emoji: String,
    @StringRes val labelRes: Int
)

object SupermarketCategories {
    val all = listOf(
        SupermarketCategory("veg", "🥬", R.string.cat_vegetables),
        SupermarketCategory("fruit", "🍎", R.string.cat_fruit),
        SupermarketCategory("meat", "🍗", R.string.cat_meat),
        SupermarketCategory("dairy", "🥛", R.string.cat_dairy),
        SupermarketCategory("frozen", "🧊", R.string.cat_frozen),
        SupermarketCategory("pantry", "🫙", R.string.cat_pantry),
        SupermarketCategory("condiment", "🧂", R.string.cat_condiments),
        SupermarketCategory("drinks", "🧃", R.string.cat_drinks)
    )

    fun forKey(key: String?): SupermarketCategory =
        all.find { it.key == key } ?: all.first { it.key == "pantry" }
}
