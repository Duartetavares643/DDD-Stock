package com.example.ddd_stock.util

object Constants {
    const val USERS_COLLECTION = "users"
    const val AUTH_SESSIONS_COLLECTION = "auth_sessions"
    const val AUTH_ERROR_LOG_COLLECTION = "auth_error_log"

    const val MAX_LOGIN_ATTEMPTS = 5
    const val MAX_PIN_ATTEMPTS = 3
    const val LOCK_DURATION_MINUTES = 30L

    const val USERNAME_MIN_LENGTH = 3
    const val USERNAME_MAX_LENGTH = 50
    const val NAME_MIN_LENGTH = 2
    const val NAME_MAX_LENGTH = 50
    const val SURNAME_MAX_LENGTH = 80
    const val PASSWORD_MIN_LENGTH = 7
    const val PASSWORD_MAX_LENGTH = 128
    const val PIN_LENGTH = 4
}
