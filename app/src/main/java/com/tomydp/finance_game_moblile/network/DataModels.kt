package com.tomydp.finance_game_moblile.network


data class LoginRequest(val email: String, val password: String)

data class LoginResponse(
    val id: Int,
    val name: String,
    val email: String,
    val token: String,
    val email_verified: Boolean
)

data class ErrorResponse(val message: String, val errors: Map<String, List<String>>?)
