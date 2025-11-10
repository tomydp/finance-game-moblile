package com.tomydp.finance_game_moblile.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tomydp.finance_game_moblile.network.ErrorResponse
import com.tomydp.finance_game_moblile.network.RegisterResponse
import com.tomydp.finance_game_moblile.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _registrationResult = MutableLiveData<Result<RegisterResponse>>()
    val registrationResult: LiveData<Result<RegisterResponse>> = _registrationResult

    fun register(name: String, email: String, password: String, passwordConfirmation: String) {
        viewModelScope.launch {
            try {
                val response = authRepository.register(name, email, password, passwordConfirmation)
                if (response.isSuccessful) {
                    _registrationResult.postValue(Result.success(response.body()!!))
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        val errorMessage = errorResponse.errors?.joinToString("\n") ?: errorResponse.message ?: "Error desconocido"
                        _registrationResult.postValue(Result.failure(Exception(errorMessage)))
                    } else {
                        _registrationResult.postValue(Result.failure(Exception("Error desconocido")))
                    }
                }
            } catch (e: Exception) {
                _registrationResult.postValue(Result.failure(e))
            }
        }
    }
}