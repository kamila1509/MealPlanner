package com.kam666.mealplanner.domain.usecase.auth

import com.kam666.mealplanner.domain.model.User
import com.kam666.mealplanner.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): User? = repository.getCurrentUser()
}
