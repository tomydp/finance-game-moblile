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

sealed class CourseProgressState {
    object Loading : CourseProgressState()
    data class Success(val progress: com.tomydp.finance_game_moblile.network.CourseProgressResponse) : CourseProgressState()
    data class Error(val message: String) : CourseProgressState()
}

sealed class CompleteCourseState {
    object Loading : CompleteCourseState()
    data class Success(val response: com.tomydp.finance_game_moblile.network.CompleteCourseResponse) : CompleteCourseState()
    data class Error(val message: String) : CompleteCourseState()
}

class CourseViewModel : ViewModel() {

    private val _courseState = MutableLiveData<CourseState>()
    val courseState: LiveData<CourseState> = _courseState

    private val _lessonState = MutableLiveData<LessonState>()
    val lessonState: LiveData<LessonState> = _lessonState

    private val _courseProgressState = MutableLiveData<CourseProgressState>()
    val courseProgressState: LiveData<CourseProgressState> = _courseProgressState

    private val _completeCourseState = MutableLiveData<CompleteCourseState>()
    val completeCourseState: LiveData<CompleteCourseState> = _completeCourseState

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

    fun getCourseProgress(courseId: Int, token: String) {
        viewModelScope.launch {
            _courseProgressState.postValue(CourseProgressState.Loading)
            try {
                val response = RetrofitClient.api.getCourseProgress(courseId, "Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        _courseProgressState.postValue(CourseProgressState.Success(it))
                    } ?: _courseProgressState.postValue(CourseProgressState.Error("Respuesta de progreso de curso inválida"))
                } else {
                    _courseProgressState.postValue(CourseProgressState.Error("Error ${response.code()} al obtener progreso del curso"))
                }
            } catch (e: Exception) {
                _courseProgressState.postValue(CourseProgressState.Error("Error de red (progreso de curso): ${e.message}"))
            }
        }
    }

    fun completeCourse(courseId: Int, token: String) {
        viewModelScope.launch {
            _completeCourseState.postValue(CompleteCourseState.Loading)
            try {
                val response = RetrofitClient.api.completeCourse(courseId, "Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        _completeCourseState.postValue(CompleteCourseState.Success(it))
                    } ?: _completeCourseState.postValue(CompleteCourseState.Error("Respuesta de completar curso inválida"))
                } else {
                    _completeCourseState.postValue(CompleteCourseState.Error("Error ${response.code()} al completar el curso"))
                }
            } catch (e: Exception) {
                _completeCourseState.postValue(CompleteCourseState.Error("Error de red (completar curso): ${e.message}"))
            }
        }
    }
}