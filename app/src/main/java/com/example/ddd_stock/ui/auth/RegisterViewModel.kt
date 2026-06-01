package com.example.ddd_stock.ui.auth
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ddd_stock.data.AppUser
import com.example.ddd_stock.data.AuthRepository
import com.example.ddd_stock.data.FirestoreRepository
import com.example.ddd_stock.data.SessionManager
import com.example.ddd_stock.util.PinUtils
import com.example.ddd_stock.util.ValidationUtils
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
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

    private fun err(msg: String?): Boolean { if (msg != null) { _authState.value = AuthState.Error(msg); return true }; return false }

    fun checkUsernameDebounced(username: String) {
        if (err(ValidationUtils.validateUsername(username))) { _usernameExists.value = null; return }
        viewModelScope.launch { _usernameExists.value = firestoreRepo.checkUsernameExists(username).getOrNull() ?: false }
    }

    fun checkEmailDebounced(email: String) {
        if (err(ValidationUtils.validateEmail(email))) { _emailExists.value = null; return }
        viewModelScope.launch { _emailExists.value = firestoreRepo.checkEmailExists(email).getOrNull() ?: false }
    }

    fun evaluatePassword(password: String) { _passwordStrength.value = ValidationUtils.evaluatePasswordStrength(password) }

    fun register(username: String, firstName: String, surname: String, email: String, contact: String, password: String, pin: String) {
        if (err(ValidationUtils.validateUsername(username)) || err(ValidationUtils.validateEmail(email)) ||
            err(ValidationUtils.validatePassword(password)) || err(ValidationUtils.validatePin(pin)) ||
            err(ValidationUtils.validateName(firstName)) || err(ValidationUtils.validateSurname(surname)) ||
            err(ValidationUtils.validateContact(contact))) return

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            if (firestoreRepo.checkUsernameExists(username).getOrNull() == true) { _authState.value = AuthState.Error("Username already taken"); return@launch }
            if (firestoreRepo.checkEmailExists(email).getOrNull() == true) { _authState.value = AuthState.Error("Email already registered"); return@launch }

            val uid = authRepo.registerWithEmail(email, password).getOrElse { _authState.value = AuthState.Error(it.message ?: "Registration failed"); return@launch }
            val (pinHash, pinSalt) = PinUtils.hashPin(pin)
            val now = Timestamp.now()

            if (firestoreRepo.createUser(AppUser(uid = uid, username = username.lowercase().trim(), firstName = firstName.trim(), surname = surname.trim(), email = email.lowercase().trim(), contact = contact.trim(), createdAt = now, updatedAt = now, lastLogin = now, pinHash = pinHash, pinSalt = pinSalt, pinCreatedAt = now, failedAttempts = 0, lockedUntil = null)).isFailure) {
                authRepo.deleteAccount()
                _authState.value = AuthState.Error("Failed to create profile"); return@launch
            }
            sessionManager.saveUserUid(uid); sessionManager.saveAuthState(true)
            _authState.value = AuthState.Success(uid)
        }
    }

    fun resetState() { _authState.value = AuthState.Idle }

    sealed class AuthState {
        data object Idle : AuthState()
        data object Loading : AuthState()
        data class Success(val uid: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
