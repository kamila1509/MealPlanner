package com.kam666.mealplanner.domain.repository

import com.kam666.mealplanner.domain.model.User

interface AuthRepository {
    fun getCurrentUser(): User?
    suspend fun signInWithGoogle(idToken: String): User
    suspend fun signOut()
}
