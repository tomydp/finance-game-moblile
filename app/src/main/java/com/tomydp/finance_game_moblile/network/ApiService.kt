package com.tomydp.finance_game_moblile.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("api/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @GET("api/analytics/rankings")
    suspend fun getAnalyticsRankings(
        @Header("Authorization") token: String
    ): Response<AnalyticsResponse>

    @GET("api/courses")
    suspend fun getCourses(@Header("Authorization") token: String): Response<CourseResponse>

    @GET("api/courses/{id}/lessons")
    suspend fun getLessons(
        @Path("id") courseId: Int,
        @Header("Authorization") token: String
    ): Response<LessonResponse>

    @GET("api/lessons/{id}/exercises")
    suspend fun getExercises(
        @Path("id") lessonId: Int,
        @Header("Authorization") token: String
    ): Response<ExerciseResponse>

    @POST("api/exercises/{id}/submit")
    suspend fun submitAnswer(@Path("id") exerciseId: Int, @Body answer: SubmitRequest, @Header("Authorization") token: String): Response<SubmitResponse>

    @POST("api/lessons/{id}/complete")
    suspend fun completeLesson(@Path("id") lessonId: Int, @Header("Authorization") token: String): Response<CompleteLessonResponse>

    @GET("api/courses/{id}/progress")
    suspend fun getCourseProgress(
        @Path("id") courseId: Int,
        @Header("Authorization") token: String
    ): Response<CourseProgressResponse>

    @POST("api/courses/{id}/complete")
    suspend fun completeCourse(
        @Path("id") courseId: Int,
        @Header("Authorization") token: String
    ): Response<CompleteCourseResponse>
}

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.110/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
