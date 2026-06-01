package com.example.ddd_stock.util
import android.util.Patterns
import java.util.regex.Pattern

object ValidationUtils {
    private val USER = Pattern.compile("^[a-zA-Z0-9]+$")
    private val NAME = Pattern.compile("^[a-zA-Z-]+$")
    private val PHONE = Pattern.compile("^\\+?[1-9]\\d{1,14}$")
    private val EMAIL = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")

    private fun checkLen(v: String, min: Int, max: Int, label: String) = when {
        v.length < min -> "$label must be at least $min characters"
        v.length > max -> "$label must be at most $max characters"
        else -> null
    }

    fun validateName(name: String, label: String = "Name", max: Int = Constants.NAME_MAX_LENGTH): String? {
        val t = name.trim(); if (t.isEmpty()) return null
        return checkLen(t, Constants.NAME_MIN_LENGTH, max, label) ?: if (!NAME.matcher(t).matches()) "$label can only contain letters and hyphens" else null
    }

    fun validateSurname(surname: String) = validateName(surname, "Surname", Constants.SURNAME_MAX_LENGTH)
    fun validateUsername(username: String): String? {
        val t = username.trim(); if (t.isEmpty()) return "Username is required"
        return checkLen(t, Constants.USERNAME_MIN_LENGTH, Constants.USERNAME_MAX_LENGTH, "Username")
            ?: if (!USER.matcher(t).matches()) "Username must be alphanumeric only" else null
    }
    fun validateEmail(email: String): String? { val t = email.trim(); return if (t.isEmpty()) "Email is required" else if (!EMAIL.matcher(t).matches()) "Invalid email format" else null }
    fun validateContact(contact: String): String? { val t = contact.trim(); return if (t.isEmpty()) null else if (!PHONE.matcher(t).matches()) "Contact must be in E.164 format (e.g. +351912345678)" else null }
    fun validatePassword(password: String): String? = when {
        password.isEmpty() -> "Password is required"
        password.length < Constants.PASSWORD_MIN_LENGTH -> "Password must be at least ${Constants.PASSWORD_MIN_LENGTH} characters"
        password.length > Constants.PASSWORD_MAX_LENGTH -> "Password must be at most ${Constants.PASSWORD_MAX_LENGTH} characters"
        !password.any { it.isLetter() } -> "Password must contain at least one letter"
        !password.any { it.isDigit() } -> "Password must contain at least one number"
        else -> null
    }
    fun validatePin(pin: String): String? {
        if (pin.length != Constants.PIN_LENGTH) return "PIN must be exactly ${Constants.PIN_LENGTH} digits"
        if (!pin.all { it.isDigit() }) return "PIN must contain only digits"
        val d = pin.map { it.digitToInt() }
        if ((0..2).any { d[it] + 1 == d[it + 1] && d[it] + 2 == d[it + 2] } || (0..2).any { d[it] - 1 == d[it + 1] && d[it] - 2 == d[it + 2] }) return "PIN cannot contain sequential digits"
        if (pin.length >= 3 && (0..pin.length - 3).any { pin.substring(it, it + 3).toSet().size == 1 }) return "PIN cannot contain repeated patterns"
        if (d.toSet().size == 1) return "PIN cannot be all the same digit"
        return null
    }

    enum class PasswordStrength { WEAK, MEDIUM, STRONG, VERY_STRONG }
    fun evaluatePasswordStrength(password: String): PasswordStrength {
        if (password.length < Constants.PASSWORD_MIN_LENGTH) return PasswordStrength.WEAK
        val s = listOf(password.length >= 10, password.length >= 14, password.any { it.isUpperCase() }, password.any { it.isLowerCase() }, password.any { it.isDigit() }, password.any { !it.isLetterOrDigit() }).count { it }
        return when { s >= 6 -> PasswordStrength.VERY_STRONG; s >= 4 -> PasswordStrength.STRONG; s >= 2 -> PasswordStrength.MEDIUM; else -> PasswordStrength.WEAK }
    }
}
