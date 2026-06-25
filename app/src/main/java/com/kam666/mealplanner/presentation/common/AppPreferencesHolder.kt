package com.kam666.mealplanner.presentation.common

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesHolder @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(loadLanguage())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _darkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, false))
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _defaultServings = MutableStateFlow(prefs.getInt(KEY_DEFAULT_SERVINGS, 2))
    val defaultServings: StateFlow<Int> = _defaultServings.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.tag).apply()
        _language.update { language }
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _darkMode.update { enabled }
    }

    fun setDefaultServings(count: Int) {
        prefs.edit().putInt(KEY_DEFAULT_SERVINGS, count).apply()
        _defaultServings.update { count }
    }

    private fun loadLanguage(): AppLanguage =
        AppLanguage.fromTag(prefs.getString(KEY_LANGUAGE, AppLanguage.ES.tag))

    companion object {
        private const val PREFS_NAME = "meal_planner_prefs"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_DEFAULT_SERVINGS = "default_servings"
    }
}
