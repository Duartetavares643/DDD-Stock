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
    fun toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "username" to username,
        "first_name" to firstName,
        "surname" to surname,
        "email" to email,
        "contact" to contact,
        "created_at" to createdAt,
        "updated_at" to updatedAt,
        "last_login" to lastLogin,
        "pin_hash" to pinHash,
        "pin_salt" to pinSalt,
        "pin_created_at" to pinCreatedAt,
        "failed_attempts" to failedAttempts,
        "locked_until" to lockedUntil
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): AppUser = AppUser(
            uid = map["uid"] as? String ?: "",
            username = map["username"] as? String ?: "",
            firstName = map["first_name"] as? String ?: "",
            surname = map["surname"] as? String ?: "",
            email = map["email"] as? String ?: "",
            contact = map["contact"] as? String ?: "",
            createdAt = map["created_at"] as? Timestamp,
            updatedAt = map["updated_at"] as? Timestamp,
            lastLogin = map["last_login"] as? Timestamp,
            pinHash = map["pin_hash"] as? String ?: "",
            pinSalt = map["pin_salt"] as? String ?: "",
            pinCreatedAt = map["pin_created_at"] as? Timestamp,
            failedAttempts = (map["failed_attempts"] as? Long)?.toInt() ?: 0,
            lockedUntil = map["locked_until"] as? Timestamp
        )
    }
}
