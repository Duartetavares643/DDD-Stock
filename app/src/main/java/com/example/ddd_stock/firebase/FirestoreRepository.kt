package com.example.ddd_stock.firebase
import com.example.ddd_stock.model.AppUser
import com.example.ddd_stock.model.AuthErrorLog
import com.example.ddd_stock.model.AuthSession
import com.example.ddd_stock.util.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val users get() = db.collection(Constants.USERS_COLLECTION)
    private val sessions get() = db.collection(Constants.AUTH_SESSIONS_COLLECTION)
    private val errors get() = db.collection(Constants.AUTH_ERROR_LOG_COLLECTION)

    private suspend fun <T> fsCall(block: suspend () -> T): Result<T> = try { Result.success(block()) } catch (e: Exception) { Result.failure(Exception(e.message)) }

    suspend fun createUser(user: AppUser) = fsCall { users.document(user.uid).set(user.toMap()).await() }
    suspend fun getUserById(uid: String) = fsCall {
        val doc = users.document(uid).get().await()
        doc.data?.let { AppUser.fromMap(it) } ?: throw Exception("User not found")
    }
    suspend fun checkUsernameExists(username: String) = fsCall { !users.whereEqualTo("username", username.lowercase().trim()).get().await().isEmpty }
    suspend fun checkEmailExists(email: String) = fsCall { !users.whereEqualTo("email", email.lowercase().trim()).get().await().isEmpty }
    suspend fun updateUser(uid: String, updates: Map<String, Any>) = fsCall { users.document(uid).set(updates, SetOptions.merge()).await() }
    suspend fun resetFailedAttempts(uid: String) = updateUser(uid, mapOf("failed_attempts" to 0, "locked_until" to null as Any))
    suspend fun updateLastLogin(uid: String) = updateUser(uid, mapOf("last_login" to Timestamp.now(), "updated_at" to Timestamp.now()))
    suspend fun createSession(session: AuthSession) = fsCall { sessions.document(session.sessionId).set(session.toMap()).await() }
    suspend fun logAuthError(errorLog: AuthErrorLog) = fsCall { errors.document(errorLog.errorId).set(errorLog.toMap()).await() }
}
