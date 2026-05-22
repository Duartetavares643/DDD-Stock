package com.example.ddd_stock.util

import java.security.MessageDigest
import java.security.SecureRandom

object PinUtils {

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPin(pin: String, salt: String? = null): Pair<String, String> {
        val actualSalt = salt ?: generateSalt()
        val hash = MessageDigest.getInstance("SHA-256")
            .digest((actualSalt + pin).toByteArray())
            .joinToString("") { "%02x".format(it) }
        return Pair(hash, actualSalt)
    }

    fun verifyPin(pin: String, storedHash: String, storedSalt: String): Boolean {
        val (hash, _) = hashPin(pin, storedSalt)
        return hash == storedHash
    }
}
