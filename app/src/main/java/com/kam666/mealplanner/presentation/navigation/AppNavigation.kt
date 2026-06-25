package com.kam666.mealplanner.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kam666.mealplanner.R
import com.kam666.mealplanner.presentation.common.AppPreferencesHolder
import com.kam666.mealplanner.presentation.common.BottomTab
import com.kam666.mealplanner.presentation.common.MealPlannerBottomBar
import com.kam666.mealplanner.presentation.ingredient.IngredientListScreen
import com.kam666.mealplanner.presentation.profile.ProfileScreen
import com.kam666.mealplanner.presentation.recipe.RecipeDetailScreen
import com.kam666.mealplanner.presentation.recipe.RecipeEditScreen
import com.kam666.mealplanner.presentation.recipe.RecipeListScreen
import com.kam666.mealplanner.presentation.aisuggestions.AiSuggestionsScreen
import com.kam666.mealplanner.presentation.importrecipe.ImportRecipeScreen
import com.kam666.mealplanner.presentation.pricepreview.MercadonaPricePreviewScreen
import com.kam666.mealplanner.presentation.shoppinglist.ShoppingListScreen
import com.kam666.mealplanner.presentation.auth.AuthViewModel
import com.kam666.mealplanner.presentation.auth.SignInScreen
import com.kam666.mealplanner.presentation.splash.SplashScreen
import com.kam666.mealplanner.presentation.weeklyplan.WeeklyPlanScreen

@Composable
fun AppNavigation(prefs: AppPreferencesHolder) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val language by prefs.language.collectAsStateWithLifecycle()
    val authViewModel: AuthViewModel = hiltViewModel()

    val showBottomBar = currentRoute in setOf(
        Screen.RecipeList.route,
        Screen.WeeklyPlan.route,
        Screen.ShoppingList.route,
        Screen.AiSuggestions.route,
        Screen.Profile.route
    )

    val showScaffold = currentRoute != Screen.Splash.route

    val selectedTab = when (currentRoute) {
        Screen.WeeklyPlan.route -> BottomTab.WeeklyPlan
        Screen.ShoppingList.route -> BottomTab.Shopping
        Screen.AiSuggestions.route -> BottomTab.AiSuggestions
        Screen.Profile.route -> BottomTab.Profile
        else -> BottomTab.Recipes
    }

    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                MealPlannerBottomBar(
                    selected = selectedTab,
                    onSelect = { tab ->
                        val route = when (tab) {
                            BottomTab.Recipes -> Screen.RecipeList.route
                            BottomTab.WeeklyPlan -> Screen.WeeklyPlan.route
                            BottomTab.Shopping -> Screen.ShoppingList.route
                            BottomTab.AiSuggestions -> Screen.AiSuggestions.route
                            BottomTab.Profile -> Screen.Profile.route
                        }
                        navController.navigate(route) {
                            popUpTo(Screen.RecipeList.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    recipesLabel = stringResource(R.string.nav_recipes),
                    planLabel = stringResource(R.string.nav_plan_short),
                    shoppingLabel = stringResource(R.string.nav_shopping),
                    profileLabel = stringResource(R.string.nav_profile)
                )
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding).fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(onSplashFinished = {
                        val dest = if (authViewModel.isSignedIn) Screen.RecipeList.route else Screen.SignIn.route
                        navController.navigate(dest) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    })
                }
                composable(Screen.SignIn.route) {
                    SignInScreen(onSignedIn = {
                        navController.navigate(Screen.RecipeList.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    })
                }
                composable(Screen.RecipeList.route) {
                    RecipeListScreen(
                        onRecipeClick = { id -> navController.navigate(Screen.RecipeDetail.createRoute(id)) },
                        onAddRecipe = { navController.navigate(Screen.RecipeEdit.createRoute()) },
                        onIngredientsClick = { navController.navigate(Screen.IngredientList.route) },
                        onImportRecipe = { navController.navigate(Screen.ImportRecipe.route) }
                    )
                }
                composable(Screen.ImportRecipe.route) {
                    ImportRecipeScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    Screen.RecipeDetail.route,
                    arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
                ) {
                    RecipeDetailScreen(
                        onEdit = { id -> navController.navigate(Screen.RecipeEdit.createRoute(id)) },
                        onBack = { navController.popBackStack() },
                        onAddToPlan = {
                            navController.navigate(Screen.WeeklyPlan.route) {
                                popUpTo(Screen.RecipeList.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(
                    Screen.RecipeEdit.route,
                    arguments = listOf(navArgument("recipeId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    })
                ) {
                    RecipeEditScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.IngredientList.route) {
                    IngredientListScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.WeeklyPlan.route) {
                    WeeklyPlanScreen(
                        language = language,
                        onGoShopping = {
                            navController.navigate(Screen.ShoppingList.route) {
                                popUpTo(Screen.RecipeList.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(Screen.ShoppingList.route) {
                    ShoppingListScreen(
                        onGoPlan = {
                            navController.navigate(Screen.WeeklyPlan.route) {
                                popUpTo(Screen.RecipeList.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onViewPrices = {
                            navController.navigate(Screen.MercadonaPricePreview.route)
                        }
                    )
                }
                composable(Screen.MercadonaPricePreview.route) {
                    MercadonaPricePreviewScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.AiSuggestions.route) {
                    AiSuggestionsScreen()
                }
                composable(Screen.Profile.route) {
                    ProfileScreen()
                }
            }
        }
    }
}
