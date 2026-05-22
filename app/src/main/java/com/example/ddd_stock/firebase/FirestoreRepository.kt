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

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val usersCollection get() = db.collection(Constants.USERS_COLLECTION)
    private val sessionsCollection get() = db.collection(Constants.AUTH_SESSIONS_COLLECTION)
    private val errorLogCollection get() = db.collection(Constants.AUTH_ERROR_LOG_COLLECTION)

    suspend fun createUser(user: AppUser): Result<Unit> {
        return try {
            usersCollection.document(user.uid).set(user.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create user profile: ${e.message}"))
        }
    }

    suspend fun getUserById(uid: String): Result<AppUser> {
        return try {
            val doc = usersCollection.document(uid).get().await()
            if (doc.exists()) {
                val data = doc.data ?: return Result.failure(Exception("User data not found"))
                Result.success(AppUser.fromMap(data))
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch user: ${e.message}"))
        }
    }

    suspend fun checkUsernameExists(username: String): Result<Boolean> {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("username", username.lowercase().trim())
                .get()
                .await()
            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to check username: ${e.message}"))
        }
    }

    suspend fun checkEmailExists(email: String): Result<Boolean> {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("email", email.lowercase().trim())
                .get()
                .await()
            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to check email: ${e.message}"))
        }
    }

    suspend fun updateUser(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(uid)
                .set(updates, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update user: ${e.message}"))
        }
    }

    suspend fun incrementFailedAttempts(uid: String, maxAttempts: Int, lockMinutes: Long): Result<Unit> {
        return try {
            val user = getUserById(uid).getOrElse { return Result.failure(Exception("User not found")) }
            val newAttempts = user.failedAttempts + 1
            val updates = mutableMapOf<String, Any>("failed_attempts" to newAttempts)
            if (newAttempts >= maxAttempts) {
                val lockDuration = lockMinutes * 60
                updates["locked_until"] = Timestamp(Timestamp.now().seconds + lockDuration, 0)
            }
            updateUser(uid, updates)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to increment attempts: ${e.message}"))
        }
    }

    suspend fun resetFailedAttempts(uid: String): Result<Unit> {
        return try {
            updateUser(uid, mapOf<String, Any>(
                "failed_attempts" to 0,
                "locked_until" to null as Any
            ))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to reset attempts: ${e.message}"))
        }
    }

    suspend fun updateLastLogin(uid: String): Result<Unit> {
        return try {
            updateUser(uid, mapOf(
                "last_login" to Timestamp.now(),
                "updated_at" to Timestamp.now()
            ))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update last login: ${e.message}"))
        }
    }

    suspend fun createSession(session: AuthSession): Result<Unit> {
        return try {
            sessionsCollection.document(session.sessionId).set(session.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create session: ${e.message}"))
        }
    }

    suspend fun logAuthError(errorLog: AuthErrorLog): Result<Unit> {
        return try {
            errorLogCollection.document(errorLog.errorId).set(errorLog.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to log error: ${e.message}"))
        }
    }

    suspend fun updatePin(uid: String, pinHash: String, pinSalt: String): Result<Unit> {
        return try {
            updateUser(uid, mapOf(
                "pin_hash" to pinHash,
                "pin_salt" to pinSalt,
                "pin_created_at" to Timestamp.now(),
                "updated_at" to Timestamp.now()
            ))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update PIN: ${e.message}"))
        }
    }

    suspend fun lockUser(uid: String, durationMinutes: Long): Result<Unit> {
        return try {
            val lockDuration = durationMinutes * 60
            updateUser(uid, mapOf(
                "locked_until" to Timestamp(Timestamp.now().seconds + lockDuration, 0),
                "updated_at" to Timestamp.now()
            ))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to lock user: ${e.message}"))
        }
    }
}
