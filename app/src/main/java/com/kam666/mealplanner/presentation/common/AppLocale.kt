package com.kam666.mealplanner.presentation.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

@SuppressLint("LocalContextConfigurationRead")
@Composable
fun AppLocale(
    preferencesHolder: AppPreferencesHolder,
    content: @Composable () -> Unit
) {
    val language = preferencesHolder.language.collectAsStateWithLifecycle().value
    val baseContext = LocalContext.current

    val localizedContext = remember(language, baseContext) {
        val config = Configuration(baseContext.resources.configuration)
        config.setLocale(Locale.forLanguageTag(language.tag))
        val contextWithConfig = baseContext.createConfigurationContext(config)
        object : ContextWrapper(contextWithConfig) {
            override fun getBaseContext(): Context = baseContext
        }
    }

    val configuration = remember(localizedContext) {
        Configuration(localizedContext.resources.configuration)
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides configuration
    ) {
        content()
    }
}

fun AppPreferencesHolder.syncAppCompatLocale() {
    AppCompatDelegate.setApplicationLocales(
        LocaleListCompat.forLanguageTags(language.value.tag)
    )
}
