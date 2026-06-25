package com.kam666.mealplanner.domain.usecase.auth

import com.kam666.mealplanner.domain.model.User
import com.kam666.mealplanner.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): User = repository.signInWithGoogle(idToken)
}
