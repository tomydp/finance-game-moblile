package com.tomydp.finance_game_moblile.lessons

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomydp.finance_game_moblile.network.Lesson
import com.tomydp.finance_game_moblile.network.LessonResponse
import com.tomydp.finance_game_moblile.network.RetrofitClient
import kotlinx.coroutines.launch

sealed class LessonState {
    object Loading : LessonState()
    data class Success(val lessons: List<Lesson>) : LessonState()
    data class Error(val message: String) : LessonState()
}

class LessonViewModel : ViewModel() {

    private val _lessonState = MutableLiveData<LessonState>()
    val lessonState: LiveData<LessonState> = _lessonState

    fun getLessons(courseId: Int) {
        _lessonState.value = LessonState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getLessons(courseId)

                if (response.isSuccessful) {
                    val body: LessonResponse? = response.body()
                    val lessons = body?.data

                    if (lessons != null) {
                        _lessonState.postValue(LessonState.Success(lessons))
                    } else {
                        _lessonState.postValue(
                            LessonState.Error("Respuesta vacía de lecciones")
                        )
                    }
                } else {
                    val errorText =
                        response.errorBody()?.string()
                            ?: "Error al obtener lecciones (HTTP ${response.code()})"
                    _lessonState.postValue(LessonState.Error(errorText))
                }
            } catch (e: Exception) {
                _lessonState.postValue(
                    LessonState.Error(e.message ?: "Error desconocido al obtener lecciones")
                )
            }
        }
    }
}
