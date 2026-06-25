package com.kam666.mealplanner.domain.usecase.mercadona

import com.kam666.mealplanner.domain.model.IngredientUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UnitConversionHelperTest {

    @Test
    fun gramsConvertedToKilograms() {
        val result = UnitConversionHelper.convert(500.0, IngredientUnit.G)
        assertNotNull(result)
        assertEquals(0.5, result!!.valueInBulkUnit, 0.001)
        assertEquals("kg", result.bulkUnit)
        assertEquals(false, result.isApproximate)
    }

    @Test
    fun kilogramsPassthrough() {
        val result = UnitConversionHelper.convert(2.0, IngredientUnit.KG)
        assertNotNull(result)
        assertEquals(2.0, result!!.valueInBulkUnit, 0.001)
        assertEquals("kg", result.bulkUnit)
    }

    @Test
    fun millilitersConvertedToLiters() {
        val result = UnitConversionHelper.convert(750.0, IngredientUnit.ML)
        assertNotNull(result)
        assertEquals(0.75, result!!.valueInBulkUnit, 0.001)
        assertEquals("l", result.bulkUnit)
    }

    @Test
    fun litersPassthrough() {
        val result = UnitConversionHelper.convert(1.5, IngredientUnit.L)
        assertNotNull(result)
        assertEquals(1.5, result!!.valueInBulkUnit, 0.001)
        assertEquals("l", result.bulkUnit)
    }

    @Test
    fun unidadPassthrough() {
        val result = UnitConversionHelper.convert(3.0, IngredientUnit.UNIDAD)
        assertNotNull(result)
        assertEquals(3.0, result!!.valueInBulkUnit, 0.001)
        assertEquals("ud", result.bulkUnit)
    }

    @Test
    fun cucharadaIsApproximateAndConvertsToLiters() {
        val result = UnitConversionHelper.convert(2.0, IngredientUnit.CUCHARADA)
        assertNotNull(result)
        assertEquals(0.030, result!!.valueInBulkUnit, 0.001)
        assertEquals("l", result.bulkUnit)
        assertEquals(true, result.isApproximate)
    }

    @Test
    fun cucharaditaIsApproximate() {
        val result = UnitConversionHelper.convert(4.0, IngredientUnit.CUCHARADITA)
        assertNotNull(result)
        assertEquals(0.020, result!!.valueInBulkUnit, 0.001)
        assertEquals("l", result.bulkUnit)
        assertEquals(true, result.isApproximate)
    }

    @Test
    fun packagesNeededCeilsCorrectly() {
        // 1.5kg required, 1.0kg package → 2 packages
        assertEquals(2, UnitConversionHelper.packagesNeeded(1.5, 1.0))
    }

    @Test
    fun packagesNeededExactFit() {
        // 2.0kg required, 1.0kg package → 2 packages
        assertEquals(2, UnitConversionHelper.packagesNeeded(2.0, 1.0))
    }

    @Test
    fun packagesNeededMinimumIsOne() {
        // Very small quantity still requires at least 1 package
        assertEquals(1, UnitConversionHelper.packagesNeeded(0.001, 5.0))
    }
}
