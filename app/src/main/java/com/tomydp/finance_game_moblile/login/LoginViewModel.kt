package com.tomydp.finance_game_moblile.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tomydp.finance_game_moblile.network.ErrorResponse
import com.tomydp.finance_game_moblile.network.LoginResponse
import com.tomydp.finance_game_moblile.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 1. Definimos los "estados" posibles de nuestra pantalla.
//    La pantalla solo puede estar en uno de estos estados a la vez.
sealed class LoginState {
    object Idle : LoginState() // Reposo
    object Loading : LoginState() // Cargando
    data class Success(val response: LoginResponse) : LoginState() // Éxito (contiene los datos del user)
    data class Error(val message: String) : LoginState() // Error (contiene el mensaje)
}

// 2. Este es el "Cerebro" (ViewModel).
//    Extiende de 'ViewModel' (la librería que importamos en Gradle).
class LoginViewModel : ViewModel() {

    // 3. El ViewModel conoce el "Almacén" (Repository)
    private val authRepository = AuthRepository()

    // 4. El "Estado" de la UI.
    //    '_loginState' es PRIVADO: solo el ViewModel puede cambiarlo.
    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    //    'loginState' es PÚBLICO: la Vista (Activity) solo puede LEERLO.
    val loginState: LiveData<LoginState> = _loginState

    // 5. La función que la Vista (Activity) llamará
    fun login(email: String, password: String) {
        // Validaciones simples
        if (email.isEmpty() || password.isEmpty()) {
            _loginState.value = LoginState.Error("Email y contraseña requeridos")
            return
        }

        // Cambiamos el estado a "Cargando"
        _loginState.value = LoginState.Loading

        // Lanzamos una corutina en el hilo del ViewModel
        viewModelScope.launch(Dispatchers.IO) { // Hilo IO para trabajo de red
            try {
                // 6. Le pedimos los datos al "Almacén" (Repository)
                val response = authRepository.login(email, password)

                // Volvemos al hilo Principal para actualizar el LiveData
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // 7. Éxito: cambiamos el estado a "Success"
                        _loginState.value = LoginState.Success(response.body()!!)
                    } else {
                        // 8. Error: parseamos el mensaje y cambiamos a "Error"
                        val errorMsg = parseErrorMessage(response.errorBody()?.string())
                        _loginState.value = LoginState.Error(errorMsg)
                    }
                }
            } catch (e: Exception) {
                // 9. Error de Red (sin conexión, etc.)
                Log.e("LOGIN_VM_ERROR", "Excepción de Red: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _loginState.value = LoginState.Error("Error de red: ${e.message}")
                }
            }
        }
    }

    // Función helper para parsear los errores de Laravel
    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody == null) return "Error desconocido"
        return try {
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            // Tu AuthController devuelve "message" para 401
            errorResponse.message ?: "Credenciales inválidas"
        } catch (e: Exception) {
            "Error al procesar la respuesta"
        }
    }
}