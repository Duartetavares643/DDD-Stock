package com.example.ddd_stock.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ddd_stock.firebase.AuthRepository
import com.example.ddd_stock.firebase.FirestoreRepository
import com.example.ddd_stock.model.AppUser
import com.example.ddd_stock.model.AuthErrorLog
import com.example.ddd_stock.model.AuthSession
import com.example.ddd_stock.service.SessionManager
import com.example.ddd_stock.util.PinUtils
import com.example.ddd_stock.util.SecurityUtils
import com.example.ddd_stock.util.ValidationUtils
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = AuthRepository()
    private val firestoreRepo = FirestoreRepository()
    private val sessionManager = SessionManager(application)

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    private val _usernameExists = MutableLiveData<Boolean?>()
    val usernameExists: LiveData<Boolean?> = _usernameExists

    private val _emailExists = MutableLiveData<Boolean?>()
    val emailExists: LiveData<Boolean?> = _emailExists

    private val _passwordStrength = MutableLiveData<ValidationUtils.PasswordStrength>()
    val passwordStrength: LiveData<ValidationUtils.PasswordStrength> = _passwordStrength

    fun checkUsernameDebounced(username: String) {
        val error = ValidationUtils.validateUsername(username)
        if (error != null) {
            _usernameExists.value = null
            return
        }
        viewModelScope.launch {
            val result = firestoreRepo.checkUsernameExists(username)
            _usernameExists.value = result.getOrNull() ?: false
        }
    }

    fun checkEmailDebounced(email: String) {
        val error = ValidationUtils.validateEmail(email)
        if (error != null) {
            _emailExists.value = null
            return
        }
        viewModelScope.launch {
            val result = firestoreRepo.checkEmailExists(email)
            _emailExists.value = result.getOrNull() ?: false
        }
    }

    fun evaluatePassword(password: String) {
        _passwordStrength.value = ValidationUtils.evaluatePasswordStrength(password)
    }

    fun register(
        username: String,
        firstName: String,
        surname: String,
        email: String,
        contact: String,
        password: String,
        pin: String
    ) {
        val usernameError = ValidationUtils.validateUsername(username)
        if (usernameError != null) {
            _authState.value = AuthState.Error(usernameError)
            return
        }
        val emailError = ValidationUtils.validateEmail(email)
        if (emailError != null) {
            _authState.value = AuthState.Error(emailError)
            return
        }
        val passwordError = ValidationUtils.validatePassword(password)
        if (passwordError != null) {
            _authState.value = AuthState.Error(passwordError)
            return
        }
        val pinError = ValidationUtils.validatePin(pin)
        if (pinError != null) {
            _authState.value = AuthState.Error(pinError)
            return
        }
        val nameError = ValidationUtils.validateName(firstName)
        if (nameError != null) {
            _authState.value = AuthState.Error(nameError)
            return
        }
        val surnameError = ValidationUtils.validateSurname(surname)
        if (surnameError != null) {
            _authState.value = AuthState.Error(surnameError)
            return
        }
        val contactError = ValidationUtils.validateContact(contact)
        if (contactError != null) {
            _authState.value = AuthState.Error(contactError)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val usernameCheck = firestoreRepo.checkUsernameExists(username)
            if (usernameCheck.getOrNull() == true) {
                _authState.value = AuthState.Error("Username already taken")
                return@launch
            }

            val emailCheck = firestoreRepo.checkEmailExists(email)
            if (emailCheck.getOrNull() == true) {
                _authState.value = AuthState.Error("Email already registered")
                return@launch
            }

            val authResult = authRepo.registerWithEmail(email, password)
            val uid = authResult.getOrElse {
                _authState.value = AuthState.Error(it.message ?: "Registration failed")
                return@launch
            }

            val (pinHash, pinSalt) = PinUtils.hashPin(pin)
            val now = Timestamp.now()

            val user = AppUser(
                uid = uid,
                username = username.lowercase().trim(),
                firstName = firstName.trim(),
                surname = surname.trim(),
                email = email.lowercase().trim(),
                contact = contact.trim(),
                createdAt = now,
                updatedAt = now,
                lastLogin = now,
                pinHash = pinHash,
                pinSalt = pinSalt,
                pinCreatedAt = now,
                failedAttempts = 0,
                lockedUntil = null
            )

            val createResult = firestoreRepo.createUser(user)
            if (createResult.isFailure) {
                authRepo.deleteAccount()
                _authState.value = AuthState.Error(createResult.exceptionOrNull()?.message ?: "Failed to create profile")
                return@launch
            }

            sessionManager.saveUserUid(uid)
            sessionManager.saveAuthState(true)
            _authState.value = AuthState.Success(uid)
        }
    }

    fun login(email: String, password: String) {
        val emailError = ValidationUtils.validateEmail(email)
        if (emailError != null) {
            _authState.value = AuthState.Error(emailError)
            return
        }
        val passwordError = ValidationUtils.validatePassword(password)
        if (passwordError != null) {
            _authState.value = AuthState.Error(passwordError)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val authResult = authRepo.loginWithEmail(email, password)
            val uid = authResult.getOrElse {
                logAuthError(
                    type = AuthErrorLog.ErrorType.INVALID_PASSWORD,
                    identifier = email
                )
                _authState.value = AuthState.Error(it.message ?: "Login failed")
                return@launch
            }

            val userResult = firestoreRepo.getUserById(uid)
            val user = userResult.getOrElse {
                _authState.value = AuthState.Error("User profile not found")
                return@launch
            }

            if (SecurityUtils.isAccountLocked(user.failedAttempts, user.lockedUntil)) {
                logAuthError(
                    type = AuthErrorLog.ErrorType.ACCOUNT_LOCKED,
                    identifier = email
                )
                _authState.value = AuthState.Error("Account locked. Try again later.")
                return@launch
            }

            firestoreRepo.resetFailedAttempts(uid)
            firestoreRepo.updateLastLogin(uid)

            val context = getApplication<Application>()
            val session = AuthSession(
                sessionId = SecurityUtils.generateSessionId(),
                uid = uid,
                createdAt = Timestamp.now(),
                expiresAt = Timestamp(Timestamp.now().seconds + 86400, 0),
                ipAddress = SecurityUtils.getDeviceIpAddress(context)
            )
            firestoreRepo.createSession(session)
            sessionManager.saveSessionId(session.sessionId)
            sessionManager.saveSessionExpiry(session.expiresAt!!)
            sessionManager.saveUserUid(uid)
            sessionManager.saveAuthState(true)

            _authState.value = AuthState.Success(uid)
        }
    }

    fun logout() {
        authRepo.signOut()
        sessionManager.clearSession()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private fun logAuthError(type: AuthErrorLog.ErrorType, identifier: String) {
        viewModelScope.launch {
            val errorLog = AuthErrorLog(
                errorId = SecurityUtils.generateErrorId(),
                errorType = type,
                identifierUsed = SecurityUtils.maskIdentifier(identifier),
                occurredAt = Timestamp.now(),
                ipAddress = SecurityUtils.getDeviceIpAddress(getApplication())
            )
            firestoreRepo.logAuthError(errorLog)
        }
    }

    sealed class AuthState {
        data object Idle : AuthState()
        data object Loading : AuthState()
        data class Success(val uid: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
