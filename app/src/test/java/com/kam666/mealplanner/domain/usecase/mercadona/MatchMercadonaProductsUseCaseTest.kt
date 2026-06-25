package com.kam666.mealplanner.domain.usecase.mercadona

import com.kam666.mealplanner.data.remote.dto.MercadonaPriceInstructionsDto
import com.kam666.mealplanner.data.remote.dto.MercadonaProductDto
import com.kam666.mealplanner.domain.model.Ingredient
import com.kam666.mealplanner.domain.model.IngredientUnit
import com.kam666.mealplanner.domain.model.PriceMatchResult
import com.kam666.mealplanner.domain.model.ShoppingListItem
import com.kam666.mealplanner.domain.repository.MercadonaRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchMercadonaProductsUseCaseTest {

    private fun product(
        id: String,
        name: String,
        unitPrice: String = "2.00",
        sizeFormat: String = "kg",
        unitSize: Double = 1.0
    ) = MercadonaProductDto(
        id = id,
        displayName = name,
        thumbnail = "https://example.com/$id.jpg",
        packaging = "Bolsa",
        priceInstructions = MercadonaPriceInstructionsDto(
            unitPrice = unitPrice,
            unitSize = unitSize,
            sizeFormat = sizeFormat
        )
    )

    private fun shoppingItem(
        name: String,
        quantity: Double,
        unit: IngredientUnit,
        category: String = "veg"
    ) = ShoppingListItem(
        ingredient = Ingredient(id = 1L, name = name, unit = unit, supermarketCategory = category),
        totalQuantity = quantity
    )

    private fun fakeRepo(searchResults: List<MercadonaProductDto>): MercadonaRepository =
        object : MercadonaRepository {
            override suspend fun getProductsForCategoryIds(categoryIds: List<Int>) = emptyList<MercadonaProductDto>()
            override suspend fun searchProducts(query: String, hitsPerPage: Int) = searchResults
        }

    @Test
    fun firstSearchResultIsUsedAsMatch() = runTest {
        val products = listOf(product("1", "Tomate pera", sizeFormat = "kg", unitSize = 1.0))
        val useCase = MatchMercadonaProductsUseCase(fakeRepo(products))
        val results = useCase(listOf(shoppingItem("tomate", 0.5, IngredientUnit.KG)))

        assertTrue(results.first() is PriceMatchResult.Matched)
        assertEquals("Tomate pera", (results.first() as PriceMatchResult.Matched).product.name)
    }

    @Test
    fun emptySearchResultsProducesNoMatch() = runTest {
        val useCase = MatchMercadonaProductsUseCase(fakeRepo(emptyList()))
        val results = useCase(listOf(shoppingItem("patata", 1.0, IngredientUnit.KG)))

        assertTrue(results.first() is PriceMatchResult.NoMatch)
    }

    @Test
    fun unitMismatchSkipsToNextCandidate() = runTest {
        val products = listOf(
            product("1", "Aceite girasol sólido", sizeFormat = "kg"),  // wrong unit for L
            product("2", "Aceite girasol botella", sizeFormat = "l")   // correct unit
        )
        val useCase = MatchMercadonaProductsUseCase(fakeRepo(products))
        val results = useCase(listOf(shoppingItem("aceite girasol", 1.0, IngredientUnit.L, "condiment")))

        val matched = results.first() as PriceMatchResult.Matched
        assertEquals("Aceite girasol botella", matched.product.name)
    }

    @Test
    fun allUnitMismatchProducesNoMatch() = runTest {
        // All products sold by weight, ingredient in liters
        val products = listOf(
            product("1", "Sal gruesa", sizeFormat = "kg"),
            product("2", "Sal fina", sizeFormat = "kg")
        )
        val useCase = MatchMercadonaProductsUseCase(fakeRepo(products))
        val results = useCase(listOf(shoppingItem("sal", 0.5, IngredientUnit.L)))

        assertTrue(results.first() is PriceMatchResult.NoMatch)
    }

    @Test
    fun totalCostCalculatedCorrectly() = runTest {
        // 500g required, product is 1kg at 2.00€ → 1 package → 2.00€
        val products = listOf(product("1", "Tomate", unitPrice = "2.00", sizeFormat = "kg", unitSize = 1.0))
        val useCase = MatchMercadonaProductsUseCase(fakeRepo(products))
        val results = useCase(listOf(shoppingItem("tomate", 500.0, IngredientUnit.G)))

        val matched = results.first() as PriceMatchResult.Matched
        assertEquals(1, matched.packagesNeeded)
        assertEquals(2.0, matched.estimatedCost, 0.001)
    }

    @Test
    fun alternativesAreStoredFromSubsequentCompatibleResults() = runTest {
        val products = listOf(
            product("1", "Leche entera", sizeFormat = "l", unitSize = 1.0),
            product("2", "Leche semidesnatada", sizeFormat = "l", unitSize = 1.0),
            product("3", "Leche desnatada", sizeFormat = "l", unitSize = 1.0)
        )
        val useCase = MatchMercadonaProductsUseCase(fakeRepo(products))
        val results = useCase(listOf(shoppingItem("leche", 2.0, IngredientUnit.L, "dairy")))

        val matched = results.first() as PriceMatchResult.Matched
        assertEquals(2, matched.alternatives.size)
    }
}
