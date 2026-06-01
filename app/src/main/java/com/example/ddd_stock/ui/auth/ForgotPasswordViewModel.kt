package com.example.ddd_stock.ui.auth
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ddd_stock.data.AuthRepository
import com.example.ddd_stock.util.ValidationUtils
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepo = AuthRepository()

    private val _resetState = MutableLiveData<ResetState>(ResetState.Idle)
    val resetState: LiveData<ResetState> = _resetState

    fun sendPasswordReset(email: String) {
        val validation = ValidationUtils.validateEmail(email)
        if (validation != null) { _resetState.value = ResetState.Error(validation); return }

        viewModelScope.launch {
            _resetState.value = ResetState.Loading
            authRepo.sendPasswordResetEmail(email).onSuccess {
                _resetState.value = ResetState.Success
            }.onFailure {
                _resetState.value = ResetState.Error(it.message ?: "Failed to send reset email")
            }
        }
    }

    fun resetState() { _resetState.value = ResetState.Idle }

    sealed class ResetState {
        data object Idle : ResetState()
        data object Loading : ResetState()
        data object Success : ResetState()
        data class Error(val message: String) : ResetState()
    }
}
