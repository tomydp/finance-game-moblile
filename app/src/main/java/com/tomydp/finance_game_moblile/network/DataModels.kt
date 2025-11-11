package com.tomydp.finance_game_moblile.network

// ---------- Auth ----------

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val id: Int,
    val name: String,
    val email: String,
    val token: String,
    val email_verified: Boolean
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String
)

data class RegisterResponse(
    val message: String
)

data class ErrorResponse(
    val message: String?,
    val errors: List<String>?
)

// ---------- Analytics ----------
// Backend: { ok, meta, weekly: { top: [...] }, global: { top: [...] } }

data class AnalyticsResponse(
    val weekly: Ranking?,
    val global: Ranking?
)

data class Ranking(
    val top: List<RankingEntry> = emptyList()
)

data class RankingEntry(
    val position: Int,
    val user: RankingUser,
    val correct: Int
)

data class RankingUser(
    val id: Int?,
    val name: String,
    val email: String?
)

// ---------- Courses ----------

data class Course(
    val id: Int,
    val name: String,
    val description: String,
    val lessons_count: Int?,
    val completed_lessons_count: Int?
)

data class CourseResponse(
    val data: List<Course>
)

// ---------- Lessons ----------
// Backend: { "data": [ { id, title, order, exercises_count, ... } ] }

data class LessonResponse(
    val data: List<Lesson>
)

data class Lesson(
    val id: Int,
    val title: String,
    val order: Int,
    val exercises_count: Int,
    val completed_exercises: Int?,
    val progress_percent: Int?
)

// ---------- Exercises ----------
// Backend: { "data": [ { ... } ] }

data class ExerciseResponse(
    val data: List<Exercise>
)

data class Exercise(
    val id: Int,
    val question: String,
    val type: String,
    val options: List<Option>,
    val correct_answer: String?,
    val explanation_md: String?,
    val has_explanation: Boolean?
)

data class Option(
    val id: Int,
    val text: String
)

// ---------- Submit Answer ----------
// Backend (de tu controlador):
// { correct, progress, completed, feedback: {explanation_md}? , explanation_md? }

data class SubmitRequest(
    val answer: Int
)

data class Feedback(
    val explanation_md: String?
)

data class SubmitResponse(
    val correct: Boolean,
    val progress: Int,
    val completed: Boolean,
    val feedback: Feedback?,
    val explanation_md: String?
)
