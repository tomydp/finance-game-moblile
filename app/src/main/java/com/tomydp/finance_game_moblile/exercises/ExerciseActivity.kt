package com.tomydp.finance_game_moblile.exercises

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tomydp.finance_game_moblile.R
import com.tomydp.finance_game_moblile.network.Exercise

class ExerciseActivity : AppCompatActivity() {

    private val viewModel: ExerciseViewModel by viewModels()
    private lateinit var tvQuestion: TextView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var btnSubmit: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var feedbackContainer: View
    private lateinit var ivFeedbackIcon: ImageView
    private lateinit var tvFeedbackTitle: TextView
    private lateinit var tvFeedbackExplanation: TextView

    private var lessonId: Int = -1
    private var exercises: List<Exercise> = emptyList()
    private var currentExerciseIndex = 0
    private var selectedOptionId: Int = -1
    private var isAnswerSubmitted = false
    private var radioButtons = mutableListOf<RadioButton>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)

        lessonId = intent.getIntExtra("LESSON_ID", -1)
        if (lessonId == -1) {
            Toast.makeText(this, "Error: Lesson ID not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        tvQuestion = findViewById(R.id.tvQuestion)
        optionsContainer = findViewById(R.id.optionsContainer)
        btnSubmit = findViewById(R.id.btnSubmit)
        progressBar = findViewById(R.id.progressBar)
        feedbackContainer = findViewById(R.id.feedbackContainer)
        ivFeedbackIcon = findViewById(R.id.ivFeedbackIcon)
        tvFeedbackTitle = findViewById(R.id.tvFeedbackTitle)
        tvFeedbackExplanation = findViewById(R.id.tvFeedbackExplanation)

        observeViewModel()
        viewModel.getExercises(lessonId)
    }

    private fun observeViewModel() {
        viewModel.exerciseState.observe(this) { state ->
            when (state) {
                is ExerciseState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is ExerciseState.Success -> {
                    progressBar.visibility = View.GONE
                    exercises = state.exercises
                    if (exercises.isNotEmpty()) {
                        displayCurrentExercise()
                    } else {
                        Toast.makeText(this, "No exercises found for this lesson.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
                is ExerciseState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun showFeedbackBanner(isCorrect: Boolean, title: String, explanation: String?) {
        feedbackContainer.setBackgroundResource(if (isCorrect) R.drawable.correct_feedback_background else R.drawable.incorrect_feedback_background)
        ivFeedbackIcon.setImageResource(if (isCorrect) R.drawable.ic_correct else R.drawable.ic_incorrect)
        tvFeedbackTitle.text = title
        tvFeedbackExplanation.text = explanation ?: ""
        tvFeedbackExplanation.visibility = if (explanation.isNullOrEmpty()) View.GONE else View.VISIBLE

        feedbackContainer.visibility = View.VISIBLE
        feedbackContainer.animate().translationY(0f).setDuration(300).start()
    }

    private fun hideFeedbackBanner() {
        feedbackContainer.animate().translationY(feedbackContainer.height.toFloat()).setDuration(300).withEndAction {
            feedbackContainer.visibility = View.GONE
        }.start()
    }

    private fun displayCurrentExercise() {
        hideFeedbackBanner()
        isAnswerSubmitted = false
        btnSubmit.visibility = View.GONE
        btnSubmit.text = "Continue"
        btnSubmit.setOnClickListener { loadNextQuestion() }

        val exercise = exercises[currentExerciseIndex]
        tvQuestion.text = exercise.question
        optionsContainer.removeAllViews()
        radioButtons.clear()

        exercise.options.forEach { option ->
            val radioButton = RadioButton(this)
            radioButton.text = option.text
            radioButton.id = View.generateViewId()
            radioButton.tag = option.id
            radioButton.setOnClickListener { handleAnswerSelected(radioButton, exercise) }
            optionsContainer.addView(radioButton)
            radioButtons.add(radioButton)
        }
    }

    private fun handleAnswerSelected(selectedRadioButton: RadioButton, exercise: Exercise) {
        if (isAnswerSubmitted) return
        isAnswerSubmitted = true

        val selectedOptionId = selectedRadioButton.tag as Int
        val selectedOptionText = selectedRadioButton.text.toString()
        val isCorrect = selectedOptionText == exercise.correct_answer

        viewModel.submitAnswer(exercise.id, selectedOptionId)

        radioButtons.forEach { it.isEnabled = false }

        if (isCorrect) {
            showFeedbackBanner(true, "¡Correcto!", exercise.explanation_md)
            selectedRadioButton.setBackgroundResource(R.drawable.btn_correct_background)
            selectedRadioButton.setTextColor(Color.WHITE)
        } else {
            showFeedbackBanner(false, "Incorrecto", exercise.explanation_md)
            selectedRadioButton.setBackgroundResource(R.drawable.btn_incorrect_background)
            selectedRadioButton.setTextColor(Color.WHITE)

            val correctButton = radioButtons.find { it.text.toString() == exercise.correct_answer }
            correctButton?.setBackgroundResource(R.drawable.btn_correct_background)
            correctButton?.setTextColor(Color.WHITE)
        }

        btnSubmit.visibility = View.VISIBLE
    }

    private fun loadNextQuestion() {
        hideFeedbackBanner()
        currentExerciseIndex++
        if (currentExerciseIndex < exercises.size) {
            displayCurrentExercise()
        } else {
            Toast.makeText(this, "Lesson completed!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}