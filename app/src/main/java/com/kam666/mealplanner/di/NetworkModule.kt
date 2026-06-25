package com.kam666.mealplanner.di

import com.kam666.mealplanner.data.remote.MercadonaApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        engine {
            connectTimeout = 15_000
            socketTimeout = 15_000
        }
    }

    @Provides
    @Singleton
    fun provideMercadonaApiService(client: HttpClient): MercadonaApiService =
        MercadonaApiService(client)
}
