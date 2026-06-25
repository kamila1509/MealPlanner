package com.kam666.mealplanner.domain.usecase.mercadona

import com.kam666.mealplanner.domain.model.IngredientUnit
import kotlin.math.ceil

object UnitConversionHelper {

    data class ConversionResult(
        val valueInBulkUnit: Double,
        val bulkUnit: String,
        val isApproximate: Boolean = false
    )

    fun convert(quantity: Double, unit: IngredientUnit): ConversionResult? = when (unit) {
        IngredientUnit.G          -> ConversionResult(quantity / 1000.0, "kg")
        IngredientUnit.KG         -> ConversionResult(quantity, "kg")
        IngredientUnit.ML         -> ConversionResult(quantity / 1000.0, "l")
        IngredientUnit.L          -> ConversionResult(quantity, "l")
        IngredientUnit.UNIDAD     -> ConversionResult(quantity, "ud")
        IngredientUnit.CUCHARADA   -> ConversionResult(quantity * 0.015, "l", isApproximate = true)
        IngredientUnit.CUCHARADITA -> ConversionResult(quantity * 0.005, "l", isApproximate = true)
        IngredientUnit.TAZA        -> ConversionResult(quantity * 0.25, "l", isApproximate = true)
        IngredientUnit.SOBRE       -> ConversionResult(quantity, "ud")
    }

    fun packagesNeeded(requiredInBulkUnit: Double, productUnitSize: Double): Int =
        ceil(requiredInBulkUnit / productUnitSize).toInt().coerceAtLeast(1)
}
