package com.tomydp.finance_game_moblile.exercises

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.tomydp.finance_game_moblile.PREFS_NAME
import com.tomydp.finance_game_moblile.R
import com.tomydp.finance_game_moblile.TOKEN_KEY
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
    private var token: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)

        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        token = sharedPrefs.getString(TOKEN_KEY, null)

        if (token == null) {
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_LONG).show()
            finish()
            return
        }

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
        token?.let { viewModel.getExercises(lessonId, it) }
    }

    private fun displayCurrentExercise() {
        hideFeedbackBanner()
        isAnswerSubmitted = false
        btnSubmit.text = "VERIFICAR"
        btnSubmit.isEnabled = false
        btnSubmit.visibility = View.VISIBLE
        btnSubmit.setOnClickListener { checkAnswer() }

        val exercise = exercises[currentExerciseIndex]
        tvQuestion.text = exercise.question
        optionsContainer.removeAllViews()
        radioButtons.clear()

        // The backend code specifies 'mcq', 'true_false', and 'fill_blank' as types.
        when (exercise.type) {
            "mcq", "true_false" -> setupMultipleChoice(exercise)
            "fill_blank" -> setupFillInTheBlank(exercise)
            else -> {
                Toast.makeText(this, "Unsupported exercise type: ${exercise.type}", Toast.LENGTH_LONG).show()
                // Optionally, skip to the next question or end the lesson
            }
        }
    }

    private fun setupMultipleChoice(exercise: Exercise) {
        val radioGroup = RadioGroup(this)
        val rgParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        radioGroup.layoutParams = rgParams
        optionsContainer.addView(radioGroup)

        exercise.options?.forEachIndexed { index, optionText ->
            val inflater = LayoutInflater.from(this)
            val radioButton = inflater.inflate(R.layout.item_exercise_option, radioGroup, false) as RadioButton

            var displayText = optionText
            if (optionText.equals("true", ignoreCase = true)) {
                displayText = "Verdadero"
            } else if (optionText.equals("false", ignoreCase = true)) {
                displayText = "Falso"
            }

            radioButton.text = displayText
            radioButton.id = View.generateViewId()
            radioButton.tag = index // Use index as the tag
            radioButton.setOnClickListener {
                btnSubmit.isEnabled = true
                selectedOptionId = radioButton.tag as Int
            }
            radioGroup.addView(radioButton)
            radioButtons.add(radioButton)
        }
    }

    private fun setupFillInTheBlank(exercise: Exercise) {
        val inflater = LayoutInflater.from(this)
        val fillBlankView = inflater.inflate(R.layout.item_exercise_fill_blank, optionsContainer, false)
        val inputAnswer = fillBlankView.findViewById<TextInputEditText>(R.id.input_answer)

        inputAnswer.doAfterTextChanged { text ->
            btnSubmit.isEnabled = !text.isNullOrBlank()
        }

        inputAnswer.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (btnSubmit.isEnabled) {
                    checkAnswer()
                }
                // Hide keyboard
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(inputAnswer.windowToken, 0)
                true
            } else {
                false
            }
        }

        optionsContainer.addView(fillBlankView)
        // Post the keyboard show action to the view's message queue to ensure it has focus.
        inputAnswer.post {
            inputAnswer.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(inputAnswer, InputMethodManager.SHOW_IMPLICIT)
        }
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
                        // This case might happen if a lesson has no exercises.
                        // We should probably just complete it and move to the next.
                        Toast.makeText(this, "Esta lección no tiene ejercicios.", Toast.LENGTH_SHORT).show()
                        token?.let { viewModel.completeLesson(lessonId, it) }
                    }
                }
                is ExerciseState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }

        viewModel.submissionState.observe(this) { state ->
            when (state) {
                is SubmissionState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    btnSubmit.isEnabled = false
                    // Disable input field for fill_blank if it exists
                    optionsContainer.findViewById<TextInputEditText>(R.id.input_answer)?.isEnabled = false
                    radioButtons.forEach { it.isEnabled = false }
                }
                is SubmissionState.Success -> {
                    progressBar.visibility = View.GONE
                    val response = state.response
                    val exercise = exercises[currentExerciseIndex]

                    if (response.correct) {
                        showFeedbackBanner(true, "¡Correcto!", response.explanation_md ?: exercise.explanation_md)
                    } else {
                        val explanation = response.explanation_md ?: exercise.explanation_md
                        showFeedbackBanner(false, "Incorrecto", explanation)
                    }

                    if (response.completed) {
                        btnSubmit.text = "FINALIZAR LECCIÓN"
                        btnSubmit.isEnabled = true
                        btnSubmit.setOnClickListener {
                            token?.let { viewModel.completeLesson(lessonId, it) }
                        }
                    } else {
                        btnSubmit.text = "CONTINUAR"
                        btnSubmit.isEnabled = true // Re-enable to allow continuing
                        btnSubmit.setOnClickListener { loadNextQuestion() }
                    }
                }
                is SubmissionState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    isAnswerSubmitted = false // Allow user to try again
                    btnSubmit.isEnabled = true // Re-enable
                    // Re-enable input field for fill_blank if it exists
                    optionsContainer.findViewById<TextInputEditText>(R.id.input_answer)?.isEnabled = true
                    radioButtons.forEach { it.isEnabled = true }
                }
                is SubmissionState.Idle -> {
                    // Do nothing
                }
            }
        }

        viewModel.lessonCompletionState.observe(this) { state ->
            Log.d("ExerciseActivity", "LessonCompletionState changed: $state")
            when (state) {
                is LessonCompletionState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is LessonCompletionState.Success -> {
                    Log.d("ExerciseActivity", "Lesson completion successful. Response: ${state.response}")
                    val nextLesson = state.response.next_lesson
                    if (nextLesson != null) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "¡Siguiente lección!", Toast.LENGTH_SHORT).show()
                        lessonId = nextLesson.id
                        currentExerciseIndex = 0
                        token?.let { viewModel.getExercises(lessonId, it) }
                    } else {
                        // Lesson finished, show a modal and return to CourseActivity
                        AlertDialog.Builder(this)
                            .setTitle("¡Lección Completada!")
                            .setMessage("¡Buen trabajo! Has completado esta lección.")
                            .setPositiveButton("CONTINUAR") { dialog, _ ->
                                dialog.dismiss()
                                finish() // This will return to CourseActivity
                            }
                            .setCancelable(false)
                            .show()
                    }
                }
                is LessonCompletionState.Error -> {
                    progressBar.visibility = View.GONE
                    Log.e("ExerciseActivity", "Lesson completion error: ${state.message}")
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    finish() // Finish on error
                }
                is LessonCompletionState.Idle -> {
                    // Do nothing
                }
            }
        }
    }

    private fun showFeedbackBanner(isCorrect: Boolean, title: String, explanation: String?) {
        val backgroundColorRes = if (isCorrect) R.drawable.correct_feedback_background else R.drawable.incorrect_feedback_background
        val textColorRes = if (isCorrect) R.color.feedback_correct_text_icon else R.color.feedback_incorrect_text_icon
        val iconRes = if (isCorrect) R.drawable.ic_correct else R.drawable.ic_incorrect

        val textColor = ContextCompat.getColor(this, textColorRes)

        feedbackContainer.setBackgroundResource(backgroundColorRes)
        ivFeedbackIcon.setImageResource(iconRes)
        ivFeedbackIcon.setColorFilter(textColor)
        tvFeedbackTitle.text = title
        tvFeedbackTitle.setTextColor(textColor)
        tvFeedbackExplanation.text = explanation ?: ""
        tvFeedbackExplanation.setTextColor(textColor)
        tvFeedbackExplanation.visibility = if (explanation.isNullOrEmpty()) View.GONE else View.VISIBLE

        feedbackContainer.visibility = View.VISIBLE
        feedbackContainer.animate().translationY(0f).setDuration(300).start()
    }

    private fun hideFeedbackBanner() {
        feedbackContainer.animate().translationY(feedbackContainer.height.toFloat()).setDuration(300).withEndAction {
            feedbackContainer.visibility = View.GONE
        }.start()
    }

    private fun checkAnswer() {
        if (isAnswerSubmitted) return
        isAnswerSubmitted = true

        val exercise = exercises[currentExerciseIndex]

        when (exercise.type) {
            "mcq", "true_false" -> checkMultipleChoiceAnswer(exercise)
            "fill_blank" -> checkFillInTheBlankAnswer(exercise)
        }
    }

    private fun checkMultipleChoiceAnswer(exercise: Exercise) {
        if (selectedOptionId == -1) {
            isAnswerSubmitted = false // allow user to try again
            return
        }

        var answerToSend = exercise.options?.getOrNull(selectedOptionId)
        if (answerToSend == null) {
            Toast.makeText(this, "Error: Opción seleccionada no válida", Toast.LENGTH_SHORT).show()
            isAnswerSubmitted = false
            return
        }

        // The backend's `checkAnswer` for `true_false` expects the normalized Spanish word.
        if (exercise.type == "true_false") {
            if (answerToSend == "true") {
                answerToSend = "verdadero"
            } else if (answerToSend == "false") {
                answerToSend = "falso"
            }
        }

        // Backend expects the string value of the answer
        token?.let { viewModel.submitAnswer(exercise.id, answerToSend, it) }
    }

    private fun checkFillInTheBlankAnswer(exercise: Exercise) {
        val inputAnswer = optionsContainer.findViewById<TextInputEditText>(R.id.input_answer)
        val userAnswer = inputAnswer.text.toString().trim()

        // Submit the string answer to the ViewModel
        token?.let { viewModel.submitAnswer(exercise.id, userAnswer, it) }
    }


    private fun loadNextQuestion() {
        hideFeedbackBanner()
        currentExerciseIndex++
        if (currentExerciseIndex < exercises.size) {
            displayCurrentExercise()
        } else {
            // Last exercise finished, try to complete the lesson
            token?.let { viewModel.completeLesson(lessonId, it) }
        }
    }
}
