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
import com.tomydp.finance_game_moblile.R
import com.tomydp.finance_game_moblile.exercises.ExerciseActivity
import com.tomydp.finance_game_moblile.network.Course

class CourseActivity : AppCompatActivity() {

    private val viewModel: CourseViewModel by viewModels()
    private lateinit var rvCourses: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var courseAdapter: CourseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        rvCourses = findViewById(R.id.rvCourses)
        progressBar = findViewById(R.id.progressBar)

        setupRecyclerView()
        observeViewModel()

        viewModel.getCourses()
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
                        viewModel.getLessonsForCourse(courseId)
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
                    val firstLesson = state.lessons.firstOrNull()
                    if (firstLesson != null) {
                        val intent = Intent(this, ExerciseActivity::class.java)
                        intent.putExtra("LESSON_ID", firstLesson.id)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "No lessons found for this course.", Toast.LENGTH_SHORT).show()
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