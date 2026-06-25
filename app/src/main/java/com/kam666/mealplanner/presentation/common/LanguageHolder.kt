package com.kam666.mealplanner.presentation.common

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

enum class AppLanguage(val tag: String) {
    ES("es"),
    EN("en");

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            if (tag == EN.tag) EN else ES
    }
}

@Singleton
class LanguageHolder @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(loadLanguage())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.tag).apply()
        _language.update { language }
    }

    private fun loadLanguage(): AppLanguage =
        AppLanguage.fromTag(prefs.getString(KEY_LANGUAGE, AppLanguage.ES.tag))

    companion object {
        private const val PREFS_NAME = "meal_planner_prefs"
        private const val KEY_LANGUAGE = "app_language"
    }
}
