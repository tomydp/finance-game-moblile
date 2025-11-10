package com.tomydp.finance_game_moblile.register

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.tomydp.finance_game_moblile.R
import com.tomydp.finance_game_moblile.repository.AuthRepository

class RegisterActivity : AppCompatActivity() {

    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val name = findViewById<EditText>(R.id.name)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val passwordConfirmation = findViewById<EditText>(R.id.password_confirmation)
        val registerButton = findViewById<Button>(R.id.register)
        val loading = findViewById<ProgressBar>(R.id.loading)

        val authRepository = AuthRepository()
        val viewModelFactory = RegisterViewModelFactory(authRepository)
        registerViewModel = ViewModelProvider(this, viewModelFactory)[RegisterViewModel::class.java]

        registerViewModel.registrationResult.observe(this) { result ->
            loading.visibility = View.GONE
            result.onSuccess {
                Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
                finish()
            }.onFailure {
                Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
            }
        }

        registerButton.setOnClickListener {
            loading.visibility = View.VISIBLE
            registerViewModel.register(
                name.text.toString(),
                email.text.toString(),
                password.text.toString(),
                passwordConfirmation.text.toString()
            )
        }
    }
}