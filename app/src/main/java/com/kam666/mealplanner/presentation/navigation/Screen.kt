package com.kam666.mealplanner.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object RecipeList : Screen("recipe_list")
    object WeeklyPlan : Screen("weekly_plan")
    object ShoppingList : Screen("shopping_list")
    object IngredientList : Screen("ingredient_list")
    object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(id: Long) = "recipe_detail/$id"
    }
    object RecipeEdit : Screen("recipe_edit?recipeId={recipeId}") {
        fun createRoute(id: Long? = null) = if (id != null) "recipe_edit?recipeId=$id" else "recipe_edit"
    }
    object MercadonaPricePreview : Screen("mercadona_price_preview")
    object AiSuggestions : Screen("ai_suggestions")
    object ImportRecipe : Screen("import_recipe")
    object Profile : Screen("profile")
    object SignIn : Screen("sign_in")
}
