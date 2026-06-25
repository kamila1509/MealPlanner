package com.kam666.mealplanner.di

import com.kam666.mealplanner.BuildConfig
import com.kam666.mealplanner.data.remote.ai.AiApiService
import com.kam666.mealplanner.data.remote.ai.GeminiApiService
import com.kam666.mealplanner.data.remote.ai.OpenAiApiService
import com.kam666.mealplanner.data.repository.AiRepositoryImpl
import com.kam666.mealplanner.domain.repository.AiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideAiApiService(httpClient: HttpClient): AiApiService =
        if (BuildConfig.USE_GEMINI)
            GeminiApiService(httpClient, BuildConfig.GEMINI_API_KEY)
        else
            OpenAiApiService(httpClient, BuildConfig.OPENAI_API_KEY)

    @Provides
    @Singleton
    fun provideAiRepository(impl: AiRepositoryImpl): AiRepository = impl
}
