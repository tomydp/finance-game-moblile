package com.tomydp.finance_game_moblile.courses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomydp.finance_game_moblile.network.Course
import com.tomydp.finance_game_moblile.network.Lesson
import com.tomydp.finance_game_moblile.network.LessonResponse
import com.tomydp.finance_game_moblile.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
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

    fun getCourses() {
        viewModelScope.launch {
            _courseState.postValue(CourseState.Loading)

            try {
                val response = RetrofitClient.api.getCourses()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        _courseState.postValue(CourseState.Success(body.data))
                    } else {
                        _courseState.postValue(CourseState.Error("Respuesta vacía"))
                    }
                } else {
                    _courseState.postValue(
                        CourseState.Error("Error ${response.code()}")
                    )
                }
            } catch (e: Exception) {
                _courseState.postValue(
                    CourseState.Error("Error: ${e.message}")
                )
            }
        }
    }


    fun getLessonsForCourse(courseId: Int) {
        viewModelScope.launch {
            _lessonState.postValue(LessonState.Loading)

            try {
                val response = RetrofitClient.api.getLessons(courseId)

                if (response.isSuccessful) {
                    val lessonResponse = response.body()

                    if (lessonResponse != null) {
                        // lessonResponse.data es List<Lesson>
                        _lessonState.postValue(
                            LessonState.Success(lessonResponse.data)
                        )
                    } else {
                        _lessonState.postValue(
                            LessonState.Error("Respuesta vacía del servidor")
                        )
                    }
                } else {
                    _lessonState.postValue(
                        LessonState.Error("Error ${response.code()}: ${response.message()}")
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _lessonState.postValue(
                    LessonState.Error("Error de red o parsing: ${e.message}")
                )
            }
        }
    }
}
