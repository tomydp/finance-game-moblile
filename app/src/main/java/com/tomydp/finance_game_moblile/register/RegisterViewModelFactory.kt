package com.tomydp.finance_game_moblile.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tomydp.finance_game_moblile.repository.AuthRepository

class RegisterViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}