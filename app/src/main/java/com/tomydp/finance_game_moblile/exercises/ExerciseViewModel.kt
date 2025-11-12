package com.tomydp.finance_game_moblile.exercises

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomydp.finance_game_moblile.network.CompleteLessonResponse
import com.tomydp.finance_game_moblile.network.Exercise
import com.tomydp.finance_game_moblile.network.RetrofitClient
import com.tomydp.finance_game_moblile.network.SubmitRequest
import com.tomydp.finance_game_moblile.network.SubmitResponse
import kotlinx.coroutines.launch

sealed class ExerciseState {
    object Loading : ExerciseState()
    data class Success(val exercises: List<Exercise>) : ExerciseState()
    data class Error(val message: String) : ExerciseState()
}

sealed class SubmissionState {
    object Idle : SubmissionState()
    object Loading : SubmissionState()
    data class Success(val response: SubmitResponse) : SubmissionState()
    data class Error(val message: String) : SubmissionState()
}

sealed class LessonCompletionState {
    object Idle : LessonCompletionState()
    object Loading : LessonCompletionState()
    data class Success(val response: CompleteLessonResponse) : LessonCompletionState()
    data class Error(val message: String) : LessonCompletionState()
}

class ExerciseViewModel : ViewModel() {

    private val _exerciseState = MutableLiveData<ExerciseState>()
    val exerciseState: LiveData<ExerciseState> = _exerciseState

    private val _submissionState = MutableLiveData<SubmissionState>(SubmissionState.Idle)
    val submissionState: LiveData<SubmissionState> = _submissionState

    private val _lessonCompletionState = MutableLiveData<LessonCompletionState>(LessonCompletionState.Idle)
    val lessonCompletionState: LiveData<LessonCompletionState> = _lessonCompletionState

    fun getExercises(lessonId: Int, token: String) {
        _exerciseState.value = ExerciseState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getExercises(lessonId, "Bearer $token")
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.data != null) {
                        _exerciseState.postValue(ExerciseState.Success(body.data))
                    }
                    else {
                        _exerciseState.postValue(ExerciseState.Error("Respuesta inválida del servidor"))
                    }
                } else {
                    _exerciseState.postValue(ExerciseState.Error("Error ${response.code()}"))
                }
            } catch (e: Exception) {
                _exerciseState.postValue(ExerciseState.Error("Error: ${e.message}"))
            }
        }
    }

    fun submitAnswer(exerciseId: Int, answer: String, token: String) {
        _submissionState.value = SubmissionState.Loading
        viewModelScope.launch {
            try {
                val request = SubmitRequest(answer = answer)
                val response = RetrofitClient.api.submitAnswer(exerciseId, request, "Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let {
                        _submissionState.postValue(SubmissionState.Success(it))
                    } ?: _submissionState.postValue(SubmissionState.Error("Respuesta vacía del servidor"))
                } else {
                    _submissionState.postValue(SubmissionState.Error("Error ${response.code()}"))
                }
            } catch (e: Exception) {
                _submissionState.postValue(SubmissionState.Error("Error: ${e.message}"))
            }
        }
    }

    fun completeLesson(lessonId: Int, token: String) {
        _lessonCompletionState.value = LessonCompletionState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.completeLesson(lessonId, "Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        _lessonCompletionState.postValue(LessonCompletionState.Success(it))
                    } ?: _lessonCompletionState.postValue(LessonCompletionState.Error("Respuesta vacía al completar la lección"))
                } else {
                    _lessonCompletionState.postValue(LessonCompletionState.Error("Error ${response.code()} al completar la lección"))
                }
            } catch (e: Exception) {
                _lessonCompletionState.postValue(LessonCompletionState.Error("Error de red al completar la lección: ${e.message}"))
            }
        }
    }
}

