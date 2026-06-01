package com.example.ddd_stock.data
import com.google.firebase.Timestamp

data class AuthErrorLog(
    val errorId: String = "",
    val errorType: ErrorType = ErrorType.INVALID_PASSWORD,
    val identifierUsed: String = "",
    val occurredAt: Timestamp? = null,
    val ipAddress: String = ""
) {
    enum class ErrorType { INVALID_IDENTITY, INVALID_PASSWORD, INVALID_PIN, ACCOUNT_LOCKED }

    fun toMap() = mapOf("error_id" to errorId, "error_type" to errorType.name, "identifier_used" to identifierUsed, "occurred_at" to occurredAt, "ip_address" to ipAddress)
    companion object {
        fun fromMap(m: Map<String, Any?>) = AuthErrorLog(
            errorId = m["error_id"] as? String ?: "",
            errorType = try { ErrorType.valueOf(m["error_type"] as? String ?: "INVALID_PASSWORD") } catch (_: Exception) { ErrorType.INVALID_PASSWORD },
            identifierUsed = m["identifier_used"] as? String ?: "",
            occurredAt = m["occurred_at"] as? Timestamp,
            ipAddress = m["ip_address"] as? String ?: ""
        )
    }
}
