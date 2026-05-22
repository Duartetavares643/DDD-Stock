package com.example.ddd_stock.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ddd_stock.firebase.FirestoreRepository
import com.example.ddd_stock.model.AppUser
import com.example.ddd_stock.service.SessionManager
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreRepo = FirestoreRepository()
    private val sessionManager = SessionManager(application)

    private val _user = MutableLiveData<AppUser>()
    val user: LiveData<AppUser> = _user

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadUserProfile() {
        val uid = sessionManager.getUserUid() ?: return
        _isLoading.value = true
        viewModelScope.launch {
            val result = firestoreRepo.getUserById(uid)
            result.onSuccess { appUser ->
                _user.value = appUser
                _isLoading.value = false
                _error.value = null
            }.onFailure {
                _isLoading.value = false
                _error.value = it.message ?: "Failed to load profile"
            }
        }
    }
}
