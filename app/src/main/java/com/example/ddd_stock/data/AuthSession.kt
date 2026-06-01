package com.example.ddd_stock.data
import com.google.firebase.Timestamp

data class AuthSession(
    val sessionId: String = "",
    val uid: String = "",
    val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null,
    val ipAddress: String = ""
) {
    fun toMap() = mapOf("session_id" to sessionId, "uid" to uid, "created_at" to createdAt, "expires_at" to expiresAt, "ip_address" to ipAddress)
    companion object {
        fun fromMap(m: Map<String, Any?>) = AuthSession(
            sessionId = m["session_id"] as? String ?: "",
            uid = m["uid"] as? String ?: "",
            createdAt = m["created_at"] as? Timestamp,
            expiresAt = m["expires_at"] as? Timestamp,
            ipAddress = m["ip_address"] as? String ?: ""
        )
    }
}
