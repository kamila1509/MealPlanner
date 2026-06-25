package com.kam666.mealplanner.domain.usecase.auth

import com.kam666.mealplanner.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() = repository.signOut()
}
