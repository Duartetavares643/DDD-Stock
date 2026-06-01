package com.example.ddd_stock.model
import com.google.firebase.Timestamp

data class AppUser(
    val uid: String = "",
    val username: String = "",
    val firstName: String = "",
    val surname: String = "",
    val email: String = "",
    val contact: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val lastLogin: Timestamp? = null,
    val pinHash: String = "",
    val pinSalt: String = "",
    val pinCreatedAt: Timestamp? = null,
    val failedAttempts: Int = 0,
    val lockedUntil: Timestamp? = null
) {
    fun toMap() = mapOf("uid" to uid, "username" to username, "first_name" to firstName, "surname" to surname, "email" to email, "contact" to contact, "created_at" to createdAt, "updated_at" to updatedAt, "last_login" to lastLogin, "pin_hash" to pinHash, "pin_salt" to pinSalt, "pin_created_at" to pinCreatedAt, "failed_attempts" to failedAttempts, "locked_until" to lockedUntil)
    companion object {
        fun fromMap(m: Map<String, Any?>)= AppUser(
            uid = m["uid"] as? String ?: "", username = m["username"] as? String ?: "", firstName = m["first_name"] as? String ?: "",
            surname = m["surname"] as? String ?: "", email = m["email"] as? String ?: "", contact = m["contact"] as? String ?: "",
            createdAt = m["created_at"] as? Timestamp, updatedAt = m["updated_at"] as? Timestamp, lastLogin = m["last_login"] as? Timestamp,
            pinHash = m["pin_hash"] as? String ?: "", pinSalt = m["pin_salt"] as? String ?: "", pinCreatedAt = m["pin_created_at"] as? Timestamp,
            failedAttempts = (m["failed_attempts"] as? Long)?.toInt() ?: 0, lockedUntil = m["locked_until"] as? Timestamp
        )
    }
}
