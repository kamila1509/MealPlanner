package com.kam666.mealplanner.domain.usecase.shoppinglist

import com.kam666.mealplanner.domain.model.*
import com.kam666.mealplanner.domain.repository.MealPlanRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GenerateShoppingListUseCaseTest {

    private val monday = LocalDate.of(2026, 6, 15)
    private val tomato = Ingredient(id = 1, name = "Tomate", unit = IngredientUnit.G)
    private val onion = Ingredient(id = 2, name = "Cebolla", unit = IngredientUnit.UNIDAD)

    @Test
    fun scalesQuantitiesByPeopleCount() = runTest {
        val recipe = recipe(
            id = 1,
            servings = 2,
            ingredients = listOf(ri(tomato, 200.0))
        )
        val plans = listOf(
            mealPlan(recipe, peopleCount = 4) // scale factor 2.0 → 400g
        )
        val result = generate(plans)

        assertEquals(1, result.size)
        assertEquals(400.0, result[0].totalQuantity, 0.001)
    }

    @Test
    fun consolidatesSameIngredientAcrossMeals() = runTest {
        val recipeA = recipe(id = 1, servings = 2, ingredients = listOf(ri(tomato, 100.0)))
        val recipeB = recipe(id = 2, servings = 4, ingredients = listOf(ri(tomato, 200.0)))
        val plans = listOf(
            mealPlan(recipeA, peopleCount = 2, date = monday),           // 100g
            mealPlan(recipeB, peopleCount = 8, date = monday.plusDays(1)) // 400g
        )
        val result = generate(plans)

        assertEquals(1, result.size)
        assertEquals(500.0, result[0].totalQuantity, 0.001)
        assertEquals("Tomate", result[0].ingredient.name)
    }

    @Test
    fun consolidatesSameIngredientInTwoRecipesOnSameDay() = runTest {
        val recipeA = recipe(id = 1, servings = 1, ingredients = listOf(ri(onion, 1.0)))
        val recipeB = recipe(id = 2, servings = 1, ingredients = listOf(ri(onion, 2.0)))
        val plans = listOf(
            mealPlan(recipeA, peopleCount = 2, date = monday),
            mealPlan(recipeB, peopleCount = 3, date = monday, mealType = MealType.CENA)
        )
        val result = generate(plans)

        assertEquals(1, result.size)
        assertEquals(8.0, result[0].totalQuantity, 0.001) // 1*2 + 2*3
    }

    @Test
    fun returnsEmptyListWhenNoMealsPlanned() = runTest {
        val result = generate(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun sortsItemsByIngredientName() = runTest {
        val recipe = recipe(
            id = 1,
            servings = 1,
            ingredients = listOf(ri(tomato, 1.0), ri(onion, 1.0))
        )
        val plans = listOf(mealPlan(recipe, peopleCount = 1))
        val result = generate(plans)

        assertEquals(listOf("Cebolla", "Tomate"), result.map { it.ingredient.name })
    }

    private fun createUseCase(plans: List<MealPlan>) = GenerateShoppingListUseCase(
        object : MealPlanRepository {
            override fun getForWeek(weekStart: LocalDate) = flowOf(plans)
            override suspend fun set(entry: MealPlan) = Unit
            override suspend fun delete(date: LocalDate, mealType: MealType) = Unit
        }
    )

    private suspend fun generate(plans: List<MealPlan>) =
        createUseCase(plans)(monday).first()

    private fun recipe(
        id: Long,
        servings: Int,
        ingredients: List<RecipeIngredient>
    ) = Recipe(
        id = id,
        name = "Receta $id",
        category = RecipeCategory.ALMUERZO,
        servings = servings,
        ingredients = ingredients
    )

    private fun ri(ingredient: Ingredient, quantity: Double) = RecipeIngredient(
        recipeId = 0,
        ingredientId = ingredient.id,
        ingredient = ingredient,
        quantity = quantity
    )

    private fun mealPlan(
        recipe: Recipe,
        peopleCount: Int,
        date: LocalDate = monday,
        mealType: MealType = MealType.ALMUERZO
    ) = MealPlan(
        id = 1,
        date = date,
        recipeId = recipe.id,
        recipe = recipe,
        peopleCount = peopleCount,
        mealType = mealType
    )
}
