package com.tomydp.finance_game_moblile.lessons

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tomydp.finance_game_moblile.R

class LessonActivity : AppCompatActivity() {

    private val viewModel: LessonViewModel by viewModels()

    private lateinit var rvLessons: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var lessonAdapter: LessonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson)

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

        viewModel.getLessons(courseId)
    }
}
