package com.example.ddd_stock.ui.auth
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ddd_stock.data.AuthRepository
import com.example.ddd_stock.data.AuthErrorLog
import com.example.ddd_stock.data.AuthSession
import com.example.ddd_stock.data.FirestoreRepository
import com.example.ddd_stock.data.SessionManager
import com.example.ddd_stock.util.Constants
import com.example.ddd_stock.util.SecurityUtils
import com.example.ddd_stock.util.ValidationUtils
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepo = AuthRepository()
    private val firestoreRepo = FirestoreRepository()
    private val sessionManager = SessionManager(application)

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    private fun err(msg: String?): Boolean { if (msg != null) { _authState.value = AuthState.Error(msg); return true }; return false }

    fun login(email: String, password: String) {
        if (err(ValidationUtils.validateEmail(email)) || err(ValidationUtils.validatePassword(password))) return

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val uid = authRepo.loginWithEmail(email, password).getOrElse {
                logAuthError(AuthErrorLog.ErrorType.INVALID_PASSWORD, email)
                _authState.value = AuthState.Error(it.message ?: "Login failed"); return@launch
            }
            val user = firestoreRepo.getUserById(uid).getOrElse { _authState.value = AuthState.Error("User profile not found"); return@launch }

            if (SecurityUtils.isAccountLocked(user.failedAttempts, user.lockedUntil)) {
                logAuthError(AuthErrorLog.ErrorType.ACCOUNT_LOCKED, email)
                _authState.value = AuthState.Error("Account locked. Try again later."); return@launch
            }

            firestoreRepo.resetFailedAttempts(uid); firestoreRepo.updateLastLogin(uid)
            val ctx = getApplication<Application>()
            val session = AuthSession(sessionId = SecurityUtils.genId(), uid = uid, createdAt = Timestamp.now(), expiresAt = Timestamp(Timestamp.now().seconds + Constants.SESSION_DURATION_SECONDS, 0), ipAddress = SecurityUtils.getDeviceIpAddress(ctx))
            firestoreRepo.createSession(session)
            sessionManager.saveSessionId(session.sessionId); sessionManager.saveSessionExpiry(session.expiresAt!!); sessionManager.saveUserUid(uid); sessionManager.saveAuthState(true)
            _authState.value = AuthState.Success(uid)
        }
    }

    fun logout() { authRepo.signOut(); sessionManager.clearSession(); _authState.value = AuthState.Idle }
    fun resetState() { _authState.value = AuthState.Idle }

    private fun logAuthError(type: AuthErrorLog.ErrorType, identifier: String) = viewModelScope.launch {
        firestoreRepo.logAuthError(AuthErrorLog(errorId = SecurityUtils.genId(), errorType = type, identifierUsed = SecurityUtils.maskIdentifier(identifier), occurredAt = Timestamp.now(), ipAddress = SecurityUtils.getDeviceIpAddress(getApplication())))
    }

    sealed class AuthState {
        data object Idle : AuthState()
        data object Loading : AuthState()
        data class Success(val uid: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
