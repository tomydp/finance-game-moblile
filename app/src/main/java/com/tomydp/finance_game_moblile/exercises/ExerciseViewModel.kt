package com.tomydp.finance_game_moblile.exercises

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class ExerciseViewModel : ViewModel() {

    private val _exerciseState = MutableLiveData<ExerciseState>()
    val exerciseState: LiveData<ExerciseState> = _exerciseState

    private val _submissionState = MutableLiveData<SubmissionState>(SubmissionState.Idle)
    val submissionState: LiveData<SubmissionState> = _submissionState

    fun getExercises(lessonId: Int) {
        _exerciseState.value = ExerciseState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getExercises(lessonId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        _exerciseState.postValue(ExerciseState.Success(body.data))
                    } else {
                        _exerciseState.postValue(
                            ExerciseState.Error("Respuesta vacía del servidor")
                        )
                    }
                } else {
                    _exerciseState.postValue(
                        ExerciseState.Error("Error ${response.code()}")
                    )
                }
            } catch (e: Exception) {
                _exerciseState.postValue(
                    ExerciseState.Error("Error: ${e.message}")
                )
            }
        }
    }

    fun submitAnswer(exerciseId: Int, answerId: Int) {
        _submissionState.value = SubmissionState.Loading

        viewModelScope.launch {
            try {
                val request = SubmitRequest(answer = answerId)
                val response = RetrofitClient.api.submitAnswer(exerciseId, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        _submissionState.postValue(
                            SubmissionState.Success(body)
                        )
                    } else {
                        _submissionState.postValue(
                            SubmissionState.Error("Respuesta vacía del servidor")
                        )
                    }
                } else {
                    _submissionState.postValue(
                        SubmissionState.Error("Error ${response.code()}")
                    )
                }
            } catch (e: Exception) {
                _submissionState.postValue(
                    SubmissionState.Error("Error: ${e.message}")
                )
            }
        }
    }
}
