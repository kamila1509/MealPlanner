package com.kam666.mealplanner.presentation.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kam666.mealplanner.R
import com.kam666.mealplanner.domain.model.IngredientUnit
import com.kam666.mealplanner.domain.model.MealType
import com.kam666.mealplanner.domain.model.RecipeCategory

@StringRes
fun RecipeCategory.labelRes(): Int = when (this) {
    RecipeCategory.DESAYUNO -> R.string.cat_desayuno
    RecipeCategory.ALMUERZO -> R.string.cat_almuerzo
    RecipeCategory.CENA -> R.string.cat_cena
    RecipeCategory.SNACK -> R.string.cat_snack
    RecipeCategory.POSTRE -> R.string.cat_postre
}

@Composable
fun RecipeCategory.localizedName(): String = stringResource(labelRes())

@StringRes
fun MealType.labelRes(): Int = when (this) {
    MealType.ALMUERZO -> R.string.meal_lunch
    MealType.CENA -> R.string.meal_dinner
}

@Composable
fun MealType.localizedName(): String = stringResource(labelRes())

@StringRes
fun IngredientUnit.labelRes(): Int = when (this) {
    IngredientUnit.G -> R.string.unit_g
    IngredientUnit.KG -> R.string.unit_kg
    IngredientUnit.ML -> R.string.unit_ml
    IngredientUnit.L -> R.string.unit_l
    IngredientUnit.UNIDAD -> R.string.unit_unit
    IngredientUnit.CUCHARADA -> R.string.unit_tbsp
    IngredientUnit.CUCHARADITA -> R.string.unit_tsp
    IngredientUnit.TAZA -> R.string.unit_taza
    IngredientUnit.SOBRE -> R.string.unit_sobre
}

@Composable
fun IngredientUnit.localizedName(): String = stringResource(labelRes())
