package com.example.ddd_stock.service
import android.content.Context
import com.google.firebase.Timestamp

class SessionManager(context: Context) {
    private val p = context.getSharedPreferences("ddd_stock_auth_prefs", Context.MODE_PRIVATE)

    private fun put(key: String, value: Any?) = p.edit().apply {
        when (value) { is String -> putString(key, value); is Boolean -> putBoolean(key, value); is Long -> putLong(key, value); is Int -> putInt(key, value) }
    }.apply()

    fun saveSessionId(id: String) = put("session_id", id)
    fun getSessionId() = p.getString("session_id", null)
    fun saveUserUid(uid: String) = put("user_uid", uid)
    fun getUserUid() = p.getString("user_uid", null)
    fun saveSessionExpiry(ts: Timestamp) = put("session_expiry", ts.seconds)
    fun isSessionValid() = p.getLong("session_expiry", 0L).let { it != 0L && Timestamp.now().seconds < it }
    fun saveAuthState(loggedIn: Boolean) = put("auth_state", loggedIn)
    fun isLoggedIn() = p.getBoolean("auth_state", false)
    fun clearSession() = p.edit().remove("session_id").remove("user_uid").remove("session_expiry").apply()
}
