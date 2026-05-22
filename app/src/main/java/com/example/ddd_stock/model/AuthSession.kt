package com.example.ddd_stock.model

import com.google.firebase.Timestamp

data class AuthSession(
    val sessionId: String = "",
    val uid: String = "",
    val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null,
    val ipAddress: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "session_id" to sessionId,
        "uid" to uid,
        "created_at" to createdAt,
        "expires_at" to expiresAt,
        "ip_address" to ipAddress
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): AuthSession = AuthSession(
            sessionId = map["session_id"] as? String ?: "",
            uid = map["uid"] as? String ?: "",
            createdAt = map["created_at"] as? Timestamp,
            expiresAt = map["expires_at"] as? Timestamp,
            ipAddress = map["ip_address"] as? String ?: ""
        )
    }
}
