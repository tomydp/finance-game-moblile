package com.tomydp.finance_game_moblile

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
        val llWeeklyRankingContainer = findViewById<LinearLayout>(R.id.llWeeklyRankingContainer)
        val llGlobalRankingContainer = findViewById<LinearLayout>(R.id.llGlobalRankingContainer)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userName = sharedPrefs.getString(USER_NAME_KEY, "Usuario")
        val token = sharedPrefs.getString(TOKEN_KEY, null)

        tvWelcome.text = "¡Hola, $userName!"

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

        if (token == null) {
            // No token, redirect to login
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

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

                    // Clear previous views
                    llWeeklyRankingContainer.removeAllViews()
                    llGlobalRankingContainer.removeAllViews()

                    // Populate Weekly Ranking
                    state.response.weekly.top.forEach { rankingEntry ->
                        val rowLayout = LinearLayout(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            orientation = LinearLayout.HORIZONTAL
                            setPadding(0, 4.dpToPx(this@MainActivity), 0, 4.dpToPx(this@MainActivity)) // Add vertical padding
                        }

                        val posTextView = TextView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            text = rankingEntry.position.toString()
                            textSize = 16f
                        }

                        val nameTextView = TextView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                3f
                            )
                            text = rankingEntry.user.name
                            textSize = 16f
                        }

                        val scoreTextView = TextView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                2f
                            )
                            text = rankingEntry.correct.toString()
                            textSize = 16f
                            gravity = View.TEXT_ALIGNMENT_VIEW_END
                        }

                        rowLayout.addView(posTextView)
                        rowLayout.addView(nameTextView)
                        rowLayout.addView(scoreTextView)
                        llWeeklyRankingContainer.addView(rowLayout)
                    }

                    // Populate Global Ranking
                    state.response.global.top.forEach { rankingEntry ->
                        val rowLayout = LinearLayout(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            orientation = LinearLayout.HORIZONTAL
                            setPadding(0, 4.dpToPx(this@MainActivity), 0, 4.dpToPx(this@MainActivity)) // Add vertical padding
                        }

                        val posTextView = TextView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            text = rankingEntry.position.toString()
                            textSize = 16f
                        }

                        val nameTextView = TextView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                3f
                            )
                            text = rankingEntry.user.name
                            textSize = 16f
                        }

                        val scoreTextView = TextView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                2f
                            )
                            text = rankingEntry.correct.toString()
                            textSize = 16f
                            gravity = View.TEXT_ALIGNMENT_VIEW_END
                        }

                        rowLayout.addView(posTextView)
                        rowLayout.addView(nameTextView)
                        rowLayout.addView(scoreTextView)
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
}

fun Int.dpToPx(context: Context): Int {
    return (context.resources.displayMetrics.density * this).toInt()
}