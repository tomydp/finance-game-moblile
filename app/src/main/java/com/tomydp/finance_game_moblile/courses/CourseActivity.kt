package com.tomydp.finance_game_moblile.courses

import android.content.Intent
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
import com.tomydp.finance_game_moblile.exercises.ExerciseActivity
import com.tomydp.finance_game_moblile.network.Course

class CourseActivity : AppCompatActivity() {

    private val viewModel: CourseViewModel by viewModels()
    private lateinit var rvCourses: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var courseAdapter: CourseAdapter
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        token = sharedPrefs.getString(TOKEN_KEY, null)

        if (token == null) {
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        rvCourses = findViewById(R.id.rvCourses)
        progressBar = findViewById(R.id.progressBar)

        setupRecyclerView()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Refresh courses when the user returns to this screen
        token?.let { viewModel.getCourses(it) }
    }

    private fun setupRecyclerView() {
        rvCourses.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModel() {
        viewModel.courseState.observe(this) { state ->
            when (state) {
                is CourseState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    rvCourses.visibility = View.GONE
                }
                is CourseState.Success -> {
                    progressBar.visibility = View.GONE
                    rvCourses.visibility = View.VISIBLE
                    courseAdapter = CourseAdapter(state.courses) { courseId ->
                        token?.let { viewModel.getLessonsForCourse(courseId, it) }
                    }
                    rvCourses.adapter = courseAdapter
                }
                is CourseState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.lessonState.observe(this) { state ->
            when (state) {
                is LessonState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is LessonState.Success -> {
                    progressBar.visibility = View.GONE
                    // Find the first lesson that is not 100% complete
                    val nextIncompleteLesson = state.lessons.firstOrNull { it.progress_percent != 100 }

                    if (nextIncompleteLesson != null) {
                        val intent = Intent(this, ExerciseActivity::class.java)
                        intent.putExtra("LESSON_ID", nextIncompleteLesson.id)
                        startActivity(intent)
                    } else if (state.lessons.isNotEmpty()) {
                        // This case means all lessons are complete
                        Toast.makeText(this, "¡Curso completado!", Toast.LENGTH_SHORT).show()
                    } else {
                        // This case means the course has no lessons
                        Toast.makeText(this, "Este curso no tiene lecciones.", Toast.LENGTH_SHORT).show()
                    }
                }
                is LessonState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}