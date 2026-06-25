package com.kam666.mealplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kam666.mealplanner.presentation.common.AppLocale
import com.kam666.mealplanner.presentation.common.AppPreferencesHolder
import com.kam666.mealplanner.presentation.common.syncAppCompatLocale
import com.kam666.mealplanner.presentation.navigation.AppNavigation
import com.kam666.mealplanner.presentation.theme.MealPlannerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefs: AppPreferencesHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs.syncAppCompatLocale()
        enableEdgeToEdge()
        setContent {
            val darkMode by prefs.darkMode.collectAsStateWithLifecycle()
            MealPlannerTheme(darkTheme = darkMode) {
                AppLocale(prefs) {
                    AppNavigation(prefs = prefs)
                }
            }
        }
    }
}
