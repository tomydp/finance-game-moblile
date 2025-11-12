package com.tomydp.finance_game_moblile.courses

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomydp.finance_game_moblile.network.Course
import com.tomydp.finance_game_moblile.network.Lesson
import com.tomydp.finance_game_moblile.network.RetrofitClient
import kotlinx.coroutines.launch

sealed class CourseState {
    object Loading : CourseState()
    data class Success(val courses: List<Course>) : CourseState()
    data class Error(val message: String) : CourseState()
}

sealed class LessonState {
    object Loading : LessonState()
    data class Success(val lessons: List<Lesson>) : LessonState()
    data class Error(val message: String) : LessonState()
}

class CourseViewModel : ViewModel() {

    private val _courseState = MutableLiveData<CourseState>()
    val courseState: LiveData<CourseState> = _courseState

    private val _lessonState = MutableLiveData<LessonState>()
    val lessonState: LiveData<LessonState> = _lessonState

    fun getCourses(token: String) {
        viewModelScope.launch {
            _courseState.postValue(CourseState.Loading)
            try {
                val response = RetrofitClient.api.getCourses("Bearer $token")
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("CourseViewModel", "Courses Response: $body")
                    if (body?.data != null) {
                        _courseState.postValue(CourseState.Success(body.data))
                    } else {
                        _courseState.postValue(CourseState.Error("Respuesta inválida del servidor (cursos)"))
                    }
                } else {
                    _courseState.postValue(CourseState.Error("Error ${response.code()} al obtener cursos"))
                }
            } catch (e: Exception) {
                _courseState.postValue(CourseState.Error("Error de red (cursos): ${e.message}"))
            }
        }
    }

    fun getLessonsForCourse(courseId: Int, token: String) {
        viewModelScope.launch {
            _lessonState.postValue(LessonState.Loading)
            try {
                val response = RetrofitClient.api.getLessons(courseId, "Bearer $token")
                if (response.isSuccessful) {
                    val lessonResponse = response.body()
                    if (lessonResponse?.data != null) {
                        _lessonState.postValue(LessonState.Success(lessonResponse.data))
                    } else {
                        _lessonState.postValue(LessonState.Error("Respuesta inválida del servidor (lecciones)"))
                    }
                } else {
                    _lessonState.postValue(LessonState.Error("Error ${response.code()} al obtener lecciones"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _lessonState.postValue(LessonState.Error("Error de red (lecciones): ${e.message}"))
            }
        }
    }
}