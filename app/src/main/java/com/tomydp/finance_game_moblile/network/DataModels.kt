package com.tomydp.finance_game_moblile.network


data class LoginRequest(val email: String, val password: String)

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

data class ErrorResponse(val message: String?, val errors: List<String>?)

data class AnalyticsResponse(
    val weekly: Ranking,
    val global: Ranking
)

data class Ranking(
    val top: List<RankingEntry>
)

data class RankingEntry(
    val position: Int,
    val user: User,
    val correct: Int
)

data class User(
    val name: String
)
