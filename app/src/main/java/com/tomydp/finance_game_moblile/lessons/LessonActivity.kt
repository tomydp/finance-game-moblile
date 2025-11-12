package com.tomydp.finance_game_moblile.lessons

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tomydp.finance_game_moblile.PREFS_NAME
import com.tomydp.finance_game_moblile.R
import com.tomydp.finance_game_moblile.TOKEN_KEY

class LessonActivity : AppCompatActivity() {

    private val viewModel: LessonViewModel by viewModels()

    private lateinit var rvLessons: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var lessonAdapter: LessonAdapter
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson)

        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        token = sharedPrefs.getString(TOKEN_KEY, null)

        if (token == null) {
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val courseId = intent.getIntExtra("COURSE_ID", -1)
        if (courseId == -1) {
            Toast.makeText(this, "Error: Course ID no encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        rvLessons = findViewById(R.id.rvLessons)
        progressBar = findViewById(R.id.progressBar)

        rvLessons.layoutManager = LinearLayoutManager(this)
        lessonAdapter = LessonAdapter(emptyList())
        rvLessons.adapter = lessonAdapter

        viewModel.lessonState.observe(this) { state ->
            when (state) {
                is LessonState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    rvLessons.visibility = View.GONE
                }

                is LessonState.Success -> {
                    progressBar.visibility = View.GONE
                    rvLessons.visibility = View.VISIBLE
                    lessonAdapter = LessonAdapter(state.lessons)
                    rvLessons.adapter = lessonAdapter
                }

                is LessonState.Error -> {
                    progressBar.visibility = View.GONE
                    rvLessons.visibility = View.VISIBLE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        token?.let { viewModel.getLessons(courseId, it) }
    }
}
