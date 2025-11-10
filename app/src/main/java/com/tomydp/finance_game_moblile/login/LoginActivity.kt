package com.tomydp.finance_game_moblile.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tomydp.finance_game_moblile.MainActivity
import com.tomydp.finance_game_moblile.PREFS_NAME
import com.tomydp.finance_game_moblile.R
import com.tomydp.finance_game_moblile.TOKEN_KEY
import com.tomydp.finance_game_moblile.USER_NAME_KEY

class LoginActivity : AppCompatActivity() {

    // --- 1. CONEXIÓN CON EL VIEWMODEL ---
    // Esta línea mágica (de la librería activity-ktx) crea
    // y conecta el "Cerebro" (LoginViewModel) a esta Vista.
    // Sobrevive a rotaciones de pantalla y maneja el ciclo de vida.
    private val viewModel: LoginViewModel by viewModels()

    // --- 2. VISTAS (UI) ---
    // (Igual que antes)
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // --- 3. ENCONTRAR VISTAS ---
        // (Igual que antes)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)

        // --- 4. CONFIGURAR LISTENERS ---
        // (Ahora es más "tonto". Solo notifica al ViewModel)
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Ya no llamamos a handleLogin() ni lanzamos corutinas.
            // Simplemente le pasamos la "orden" al Cerebro.
            viewModel.login(email, password)
        }

        // --- 5. OBSERVAR EL ESTADO ---
        // ¡Aquí está la magia!
        // Nos suscribimos a los cambios de 'loginState' del ViewModel.
        // Este bloque se ejecutará CADA VEZ que el ViewModel cambie el estado.
        observeLoginState()
    }

    private fun observeLoginState() {
        viewModel.loginState.observe(this) { state ->
            // Reaccionamos al estado que nos envía el ViewModel
            when (state) {
                is LoginState.Loading -> {
                    // Estado: Cargando
                    setLoading(true)
                }
                is LoginState.Success -> {
                    // Estado: Éxito
                    setLoading(false)

                    // Los datos vienen en el objeto 'state'
                    val userName = state.response.name
                    val token = state.response.token

                    saveLoginData(token, userName)

                    Toast.makeText(this, "¡Bienvenido $userName!", Toast.LENGTH_LONG).show()

                    // Navegamos al Home
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Cerramos la pantalla de Login
                }
                is LoginState.Error -> {
                    // Estado: Error
                    setLoading(false)
                    // El mensaje de error viene del ViewModel
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                is LoginState.Idle -> {
                    // Estado: Reposo (inicial)
                    setLoading(false)
                }
            }
        }
    }

    // --- 6. FUNCIONES HELPER (DE LA VISTA) ---

    // Esta función se queda aquí, porque controla la UI (Vistas)
    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
    }

    // Esta función se queda aquí, porque maneja almacenamiento local (Context)
    private fun saveLoginData(token: String, userName: String) {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString(TOKEN_KEY, token)
            putString(USER_NAME_KEY, userName)
            apply()
        }
    }

    // ¡handleLogin() y parseErrorMessage() DESAPARECIERON!
    // Su lógica ahora vive en el LoginViewModel.
}