package com.example.ddd_stock.util

import android.util.Patterns
import java.util.regex.Pattern
import com.example.ddd_stock.util.Constants.USERNAME_MIN_LENGTH
import com.example.ddd_stock.util.Constants.USERNAME_MAX_LENGTH
import com.example.ddd_stock.util.Constants.NAME_MIN_LENGTH
import com.example.ddd_stock.util.Constants.NAME_MAX_LENGTH
import com.example.ddd_stock.util.Constants.SURNAME_MAX_LENGTH
import com.example.ddd_stock.util.Constants.PASSWORD_MIN_LENGTH
import com.example.ddd_stock.util.Constants.PASSWORD_MAX_LENGTH
import com.example.ddd_stock.util.Constants.PIN_LENGTH

object ValidationUtils {

    private val USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$")
    private val NAME_PATTERN = Pattern.compile("^[a-zA-Z-]+$")
    private val E164_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$")
    private val RFC5322_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    )

    fun validateUsername(username: String): String? {
        val trimmed = username.trim()
        if (trimmed.isEmpty()) return "Username is required"
        if (trimmed.length < USERNAME_MIN_LENGTH) return "Username must be at least $USERNAME_MIN_LENGTH characters"
        if (trimmed.length > USERNAME_MAX_LENGTH) return "Username must be at most $USERNAME_MAX_LENGTH characters"
        if (!USERNAME_PATTERN.matcher(trimmed).matches()) return "Username must be alphanumeric only"
        if (trimmed.contains(" ")) return "Username cannot contain spaces"
        return null
    }

    fun validateName(name: String): String? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.length < NAME_MIN_LENGTH) return "Name must be at least $NAME_MIN_LENGTH characters"
        if (trimmed.length > NAME_MAX_LENGTH) return "Name must be at most $NAME_MAX_LENGTH characters"
        if (!NAME_PATTERN.matcher(trimmed).matches()) return "Name can only contain letters and hyphens"
        return null
    }

    fun validateSurname(surname: String): String? {
        val trimmed = surname.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.length < NAME_MIN_LENGTH) return "Surname must be at least $NAME_MIN_LENGTH characters"
        if (trimmed.length > SURNAME_MAX_LENGTH) return "Surname must be at most $SURNAME_MAX_LENGTH characters"
        if (!NAME_PATTERN.matcher(trimmed).matches()) return "Surname can only contain letters and hyphens"
        return null
    }

    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return "Email is required"
        if (!RFC5322_PATTERN.matcher(trimmed).matches()) return "Invalid email format"
        return null
    }

    fun validateContact(contact: String): String? {
        val trimmed = contact.trim()
        if (trimmed.isEmpty()) return null
        if (!E164_PATTERN.matcher(trimmed).matches()) return "Contact must be in E.164 format (e.g. +351912345678)"
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.isEmpty()) return "Password is required"
        if (password.length < PASSWORD_MIN_LENGTH) return "Password must be at least $PASSWORD_MIN_LENGTH characters"
        if (password.length > PASSWORD_MAX_LENGTH) return "Password must be at most $PASSWORD_MAX_LENGTH characters"
        if (!password.any { it.isLetter() }) return "Password must contain at least one letter"
        if (!password.any { it.isDigit() }) return "Password must contain at least one number"
        return null
    }

    fun validatePin(pin: String): String? {
        if (pin.length != PIN_LENGTH) return "PIN must be exactly $PIN_LENGTH digits"
        if (!pin.all { it.isDigit() }) return "PIN must contain only digits"

        val digits = pin.map { it.digitToInt() }

        val isSequential = (0..2).any { i ->
            (digits[i] + 1 == digits[i + 1]) && (digits[i] + 2 == digits[i + 2])
        } || (0..2).any { i ->
            (digits[i] - 1 == digits[i + 1]) && (digits[i] - 2 == digits[i + 2])
        }
        if (isSequential) return "PIN cannot contain sequential digits"

        val hasRepeatedPattern = pin.length >= 3 && (0..pin.length - 3).any { i ->
            pin.substring(i, i + 3).toSet().size == 1
        }
        if (hasRepeatedPattern) return "PIN cannot contain repeated patterns"

        if (digits.toSet().size == 1) return "PIN cannot be all the same digit"

        return null
    }

    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG, VERY_STRONG
    }

    fun evaluatePasswordStrength(password: String): PasswordStrength {
        if (password.length < PASSWORD_MIN_LENGTH) return PasswordStrength.WEAK

        var score = 0
        if (password.length >= 10) score++
        if (password.length >= 14) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score >= 6 -> PasswordStrength.VERY_STRONG
            score >= 4 -> PasswordStrength.STRONG
            score >= 2 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
}
