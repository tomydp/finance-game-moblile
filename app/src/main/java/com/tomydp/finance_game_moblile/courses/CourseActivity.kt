package com.tomydp.finance_game_moblile.courses

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import android.util.Log // Added this import
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
    private var currentCourseId: Int? = null

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
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the activity's intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val courseIdToComplete = intent.getIntExtra("COMPLETE_COURSE_ID", -1)
        if (courseIdToComplete != -1) {
            Log.d("CourseActivity", "Received intent to complete course ID: $courseIdToComplete")
            token?.let { viewModel.completeCourse(courseIdToComplete, it) }
            // Remove the extra to prevent re-triggering on configuration change
            getIntent().removeExtra("COMPLETE_COURSE_ID")
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh courses when the user returns to this screen
        token?.let { viewModel.getCourses(it) }
    }

    private fun setupRecyclerView() {
        rvCourses.layoutManager = LinearLayoutManager(this)
        courseAdapter = CourseAdapter(emptyList()) { courseId ->
            currentCourseId = courseId
            // When a course is clicked, we no longer need to check progress first,
            // we can directly try to get the lessons.
            token?.let { viewModel.getLessonsForCourse(courseId, it) }
        }
        rvCourses.adapter = courseAdapter
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
                    courseAdapter.submitList(state.courses)
                }
                is CourseState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.courseProgressState.observe(this) { state ->
            when (state) {
                is CourseProgressState.Loading -> progressBar.visibility = View.VISIBLE
                is CourseProgressState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                is CourseProgressState.Success -> {
                    // This logic is now simplified, as getCourses provides all the info.
                    // We just proceed to get the lessons.
                    currentCourseId?.let { courseId ->
                        token?.let { token ->
                            viewModel.getLessonsForCourse(courseId, token)
                        }
                    }
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
                    val nextIncompleteLesson = state.lessons.firstOrNull { it.progress_percent != 100 }

                    if (nextIncompleteLesson != null) {
                        val intent = Intent(this, ExerciseActivity::class.java)
                        intent.putExtra("COURSE_ID", currentCourseId)
                        intent.putExtra("LESSON_ID", nextIncompleteLesson.id)
                        intent.putExtra("LESSON_NAME", nextIncompleteLesson.title)
                        startActivity(intent)
                    } else if (state.lessons.isNotEmpty()) {
                        currentCourseId?.let { courseId ->
                            token?.let { token ->
                                viewModel.completeCourse(courseId, token)
                            }
                        }
                    } else {
                        Toast.makeText(this, "Este curso no tiene lecciones.", Toast.LENGTH_SHORT).show()
                    }
                }
                is LessonState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.completeCourseState.observe(this) { state ->
            when (state) {
                is CompleteCourseState.Loading -> progressBar.visibility = View.VISIBLE
                is CompleteCourseState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                is CompleteCourseState.Success -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "¡Curso completado!", Toast.LENGTH_SHORT).show()
                    token?.let { viewModel.getCourses(it) }
                }
            }
        }
    }
}