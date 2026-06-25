package com.kam666.mealplanner.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.kam666.mealplanner.domain.model.User
import com.kam666.mealplanner.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth?,
    private val googleSignInClient: GoogleSignInClient?
) : AuthRepository {

    override fun getCurrentUser(): User? = firebaseAuth?.currentUser?.toDomain()

    override suspend fun signInWithGoogle(idToken: String): User {
        val auth = firebaseAuth ?: throw IllegalStateException("Firebase no configurado. Añade google-services.json.")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user?.toDomain() ?: throw Exception("Sign in failed")
    }

    override suspend fun signOut() {
        firebaseAuth?.signOut()
        googleSignInClient?.signOut()?.await()
    }
}

private fun FirebaseUser.toDomain() = User(
    uid = uid,
    email = email,
    displayName = displayName,
    photoUrl = photoUrl?.toString()
)
