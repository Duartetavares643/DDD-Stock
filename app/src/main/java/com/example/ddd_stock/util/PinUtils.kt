package com.example.ddd_stock.util
import java.security.MessageDigest
import java.security.SecureRandom

object PinUtils {
    private fun generateSalt() = SecureRandom().run { val b = ByteArray(16); nextBytes(b); b.joinToString("") { "%02x".format(it) } }
    fun hashPin(pin: String, salt: String? = null): Pair<String, String> {
        val s = salt ?: generateSalt()
        return Pair(MessageDigest.getInstance("SHA-256").digest((s + pin).toByteArray()).joinToString("") { "%02x".format(it) }, s)
    }
    fun verifyPin(pin: String, storedHash: String, storedSalt: String) = hashPin(pin, storedSalt).first == storedHash
}
