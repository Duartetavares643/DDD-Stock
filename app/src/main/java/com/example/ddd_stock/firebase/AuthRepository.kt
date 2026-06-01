package com.example.ddd_stock.firebase
import com.google.firebase.auth.*
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    private suspend fun <T> firebaseCall(block: suspend () -> T): Result<T> = try { Result.success(block()) }
    catch (e: FirebaseAuthWeakPasswordException) { Result.failure(Exception("Password is too weak")) }
    catch (e: FirebaseAuthUserCollisionException) { Result.failure(Exception("An account with this email already exists")) }
    catch (e: FirebaseAuthInvalidCredentialsException) { Result.failure(Exception("Invalid email or password")) }
    catch (e: FirebaseAuthInvalidUserException) { Result.failure(Exception("No account found with this email")) }
    catch (e: Exception) { Result.failure(Exception(e.message ?: "Auth failed")) }

    suspend fun registerWithEmail(email: String, password: String) = firebaseCall { auth.createUserWithEmailAndPassword(email, password).await().user?.uid ?: throw Exception("Failed to create user") }
    suspend fun loginWithEmail(email: String, password: String) = firebaseCall { auth.signInWithEmailAndPassword(email, password).await().user?.uid ?: throw Exception("Failed to sign in") }
    fun getCurrentUserId() = auth.currentUser?.uid
    fun isUserLoggedIn() = auth.currentUser != null
    fun signOut() = auth.signOut()
    suspend fun deleteAccount() = firebaseCall { auth.currentUser?.delete()?.await() }
}
