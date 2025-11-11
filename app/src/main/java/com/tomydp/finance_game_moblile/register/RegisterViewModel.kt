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
                    val body = response.body()
                    if (body != null) {
                        _registrationResult.postValue(Result.success(body))
                    } else {
                        _registrationResult.postValue(Result.failure(Exception("Respuesta vacía del servidor")))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = parseError(errorBody)
                    _registrationResult.postValue(Result.failure(Exception(message)))
                }
            } catch (e: Exception) {
                _registrationResult.postValue(Result.failure(e))
            }
        }
    }

    // --- Función auxiliar para manejar errores sin romper con Gson ---
    private fun parseError(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) {
            return "Error en el servidor"
        }

        return try {
            val errorResponse = Gson().fromJson(
                errorBody,
                com.tomydp.finance_game_moblile.network.ErrorResponse::class.java
            )

            errorResponse.message
                ?: errorResponse.errors?.joinToString("\n")
                ?: errorBody
        } catch (e: Exception) {
            // Si no es JSON válido, devolvemos el texto tal cual en vez de crashear
            errorBody
        }
    }
}
