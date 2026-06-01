package com.example.ddd_stock.util
import android.content.Context
import java.net.NetworkInterface
import java.util.UUID
import com.google.firebase.Timestamp

object SecurityUtils {
    fun genId() = UUID.randomUUID().toString()
    fun maskIdentifier(id: String): String {
        if (id.length <= 3) return "${id.first()}${"*".repeat(id.length - 1)}"
        return "${id.first()}${"*".repeat(id.length - 2)}${id.last()}"
    }
    fun getDeviceIpAddress(context: Context): String {
        try {
            val ni = NetworkInterface.getNetworkInterfaces() ?: return "0.0.0.0"
            while (ni.hasMoreElements()) {
                val addrs = ni.nextElement().inetAddresses ?: continue
                while (addrs.hasMoreElements()) {
                    val a = addrs.nextElement()
                    if (!a.isLoopbackAddress) { val h = a.hostAddress ?: continue; if (':' !in h) return h }
                }
            }
        } catch (_: Exception) {}
        return "0.0.0.0"
    }
    fun isAccountLocked(failedAttempts: Int, lockedUntil: Timestamp?) =
        lockedUntil != null && failedAttempts >= Constants.MAX_LOGIN_ATTEMPTS && Timestamp.now().seconds < lockedUntil.seconds
}
