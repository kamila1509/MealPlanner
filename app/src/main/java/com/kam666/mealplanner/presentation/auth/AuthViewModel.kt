package com.kam666.mealplanner.presentation.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.kam666.mealplanner.domain.usecase.auth.GetCurrentUserUseCase
import com.kam666.mealplanner.domain.usecase.auth.SignInWithGoogleUseCase
import com.kam666.mealplanner.domain.usecase.auth.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val signOut: SignOutUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val googleSignInClient: GoogleSignInClient?
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isSignedIn: Boolean get() = getCurrentUser() != null

    fun getSignInIntent(): Intent? = googleSignInClient?.signInIntent

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                    ?: throw Exception("No se obtuvo el token de Google")
                val user = signInWithGoogle(idToken)
                _uiState.value = AuthUiState.Success(user)
            } catch (e: ApiException) {
                _uiState.value = AuthUiState.Error("Error de Google Sign-In: ${e.statusCode}")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun handleSignOut() {
        viewModelScope.launch {
            signOut()
            _uiState.value = AuthUiState.Idle
        }
    }
}
