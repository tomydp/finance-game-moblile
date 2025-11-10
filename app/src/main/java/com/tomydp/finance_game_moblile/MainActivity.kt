package com.tomydp.finance_game_moblile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tomydp.finance_game_moblile.login.LoginActivity

// Nota: Las constantes (PREFS_NAME, TOKEN_KEY, USER_NAME_KEY)
// se leen automáticamente desde AppConstants.kt, por eso no están aquí.

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Conectamos este Kotlin con el XML de abajo
        setContentView(R.layout.activity_main)

        // 1. Encontrar los elementos del diseño
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // 2. Leer los datos guardados (ahora usa las constantes de AppConstants.kt)
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userName = sharedPrefs.getString(USER_NAME_KEY, "Usuario") // "Usuario" es el valor por defecto

        // 3. Mostrar el saludo
        tvWelcome.text = "¡Hola, $userName!"

        // 4. Configurar el botón de Logout
        btnLogout.setOnClickListener {
            // Borramos los datos de la sesión
            sharedPrefs.edit().apply {
                remove(TOKEN_KEY)
                remove(USER_NAME_KEY)
                apply()
            }

            // Enviamos al usuario de vuelta al Login
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish() // Cerramos esta pantalla (Home) para que no pueda volver con "Atrás"
        }
    }
}