package com.example.ddd_stock.util

import android.content.Context
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.UUID

object SecurityUtils {

    fun generateSessionId(): String = UUID.randomUUID().toString()

    fun generateErrorId(): String = UUID.randomUUID().toString()

    fun maskIdentifier(identifier: String): String {
        if (identifier.length <= 3) return identifier.first().toString().padEnd(identifier.length, '*')
        val first = identifier.first()
        val last = identifier.last()
        val masked = identifier.drop(1).dropLast(1).map { '*' }.joinToString("")
        return "$first$masked$last"
    }

    fun getDeviceIpAddress(context: Context): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is InetAddress) {
                        val hostAddress = address.hostAddress ?: continue
                        if (hostAddress.indexOf(':') < 0) return hostAddress
                    }
                }
            }
        } catch (_: Exception) {}
        return "0.0.0.0"
    }

    fun isAccountLocked(failedAttempts: Int, lockedUntil: com.google.firebase.Timestamp?): Boolean {
        if (lockedUntil == null) return false
        if (failedAttempts < Constants.MAX_LOGIN_ATTEMPTS) return false
        return com.google.firebase.Timestamp.now().seconds < lockedUntil.seconds
    }

}
