package com.example.ddd_stock.service

import android.content.Context
import android.content.SharedPreferences
import com.example.ddd_stock.model.AuthSession
import com.google.firebase.Timestamp

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSessionId(sessionId: String) {
        prefs.edit().putString(KEY_SESSION_ID, sessionId).apply()
    }

    fun getSessionId(): String? = prefs.getString(KEY_SESSION_ID, null)

    fun saveUserUid(uid: String) {
        prefs.edit().putString(KEY_USER_UID, uid).apply()
    }

    fun getUserUid(): String? = prefs.getString(KEY_USER_UID, null)

    fun saveSessionExpiry(timestamp: Timestamp) {
        prefs.edit().putLong(KEY_SESSION_EXPIRY, timestamp.seconds).apply()
    }

    fun isSessionValid(): Boolean {
        val expiry = prefs.getLong(KEY_SESSION_EXPIRY, 0L)
        if (expiry == 0L) return false
        val now = Timestamp.now().seconds
        return now < expiry
    }

    fun clearSession() {
        prefs.edit().remove(KEY_SESSION_ID)
            .remove(KEY_USER_UID)
            .remove(KEY_SESSION_EXPIRY)
            .apply()
    }

    fun saveAuthState(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_AUTH_STATE, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_AUTH_STATE, false)

    companion object {
        private const val PREFS_NAME = "ddd_stock_auth_prefs"
        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_USER_UID = "user_uid"
        private const val KEY_SESSION_EXPIRY = "session_expiry"
        private const val KEY_AUTH_STATE = "auth_state"
    }
}
