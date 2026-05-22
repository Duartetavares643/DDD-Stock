package com.example.ddd_stock.model

import com.google.firebase.Timestamp

data class AuthErrorLog(
    val errorId: String = "",
    val errorType: ErrorType = ErrorType.INVALID_PASSWORD,
    val identifierUsed: String = "",
    val occurredAt: Timestamp? = null,
    val ipAddress: String = ""
) {
    enum class ErrorType {
        INVALID_IDENTITY,
        INVALID_PASSWORD,
        INVALID_PIN,
        ACCOUNT_LOCKED
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "error_id" to errorId,
        "error_type" to errorType.name,
        "identifier_used" to identifierUsed,
        "occurred_at" to occurredAt,
        "ip_address" to ipAddress
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): AuthErrorLog = AuthErrorLog(
            errorId = map["error_id"] as? String ?: "",
            errorType = try {
                ErrorType.valueOf(map["error_type"] as? String ?: "INVALID_PASSWORD")
            } catch (_: Exception) { ErrorType.INVALID_PASSWORD },
            identifierUsed = map["identifier_used"] as? String ?: "",
            occurredAt = map["occurred_at"] as? Timestamp,
            ipAddress = map["ip_address"] as? String ?: ""
        )
    }
}
