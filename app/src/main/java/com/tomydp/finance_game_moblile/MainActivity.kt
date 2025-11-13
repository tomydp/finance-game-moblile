package com.tomydp.finance_game_moblile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.tomydp.finance_game_moblile.login.LoginActivity
import com.tomydp.finance_game_moblile.repository.AuthRepository
import com.tomydp.finance_game_moblile.viewmodel.AnalyticsState
import com.tomydp.finance_game_moblile.viewmodel.AnalyticsViewModel
import com.tomydp.finance_game_moblile.viewmodel.AnalyticsViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var analyticsViewModel: AnalyticsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnProfile = findViewById<Button>(R.id.btnProfile)
        val llWeeklyRankingContainer = findViewById<LinearLayout>(R.id.llWeeklyRankingContainer)
        val llGlobalRankingContainer = findViewById<LinearLayout>(R.id.llGlobalRankingContainer)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userName = sharedPrefs.getString(USER_NAME_KEY, "Usuario")
        val token = sharedPrefs.getString(TOKEN_KEY, null)

        tvWelcome.text = "¡Hola, $userName!"

        btnPlay.setOnClickListener {
            val intent = Intent(this@MainActivity, com.tomydp.finance_game_moblile.courses.CourseActivity::class.java)
            startActivity(intent)
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this@MainActivity, com.tomydp.finance_game_moblile.profile.ProfileActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            sharedPrefs.edit().apply {
                remove(TOKEN_KEY)
                remove(USER_NAME_KEY)
                apply()
            }

            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Si no hay token, redirigir a login
        if (token == null) {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Crear ViewModel
        val authRepository = AuthRepository()
        val viewModelFactory = AnalyticsViewModelFactory(authRepository)
        analyticsViewModel = ViewModelProvider(this, viewModelFactory)[AnalyticsViewModel::class.java]

        analyticsViewModel.analyticsState.observe(this) { state ->
            when (state) {
                is AnalyticsState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }

                is AnalyticsState.Success -> {
                    progressBar.visibility = View.GONE

                    // Limpiar vistas anteriores
                    llWeeklyRankingContainer.removeAllViews()
                    llGlobalRankingContainer.removeAllViews()

                    // ✅ Weekly Ranking (seguro contra null)
                    val weeklyTop = state.response.weekly?.top ?: emptyList()
                    weeklyTop.forEach { rankingEntry ->
                        val rowLayout = createRankingRow(
                            position = rankingEntry.position,
                            name = rankingEntry.user.name,
                            score = rankingEntry.correct
                        )
                        llWeeklyRankingContainer.addView(rowLayout)
                    }

                    // ✅ Global Ranking (seguro contra null)
                    val globalTop = state.response.global?.top ?: emptyList()
                    globalTop.forEach { rankingEntry ->
                        val rowLayout = createRankingRow(
                            position = rankingEntry.position,
                            name = rankingEntry.user.name,
                            score = rankingEntry.correct
                        )
                        llGlobalRankingContainer.addView(rowLayout)
                    }
                }

                is AnalyticsState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        analyticsViewModel.getAnalytics(token)
    }

    // 🧩 Función auxiliar para crear una fila de ranking reutilizable
    private fun createRankingRow(position: Int, name: String, score: Int): LinearLayout {
        val rowLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 4.dpToPx(this@MainActivity), 0, 4.dpToPx(this@MainActivity))
        }

        val posTextView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = position.toString()
            textSize = 16f
        }

        val nameTextView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3f)
            text = name
            textSize = 16f
        }

        val scoreTextView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
            text = score.toString()
            textSize = 16f
            gravity = Gravity.END
        }

        rowLayout.addView(posTextView)
        rowLayout.addView(nameTextView)
        rowLayout.addView(scoreTextView)

        return rowLayout
    }
}

fun Int.dpToPx(context: Context): Int {
    return (context.resources.displayMetrics.density * this).toInt()
}
