package com.tomydp.finance_game_moblile.repository

import com.tomydp.finance_game_moblile.network.ApiService
import com.tomydp.finance_game_moblile.network.LoginRequest
import com.tomydp.finance_game_moblile.network.LoginResponse
import com.tomydp.finance_game_moblile.network.RetrofitClient
import retrofit2.Response

class AuthRepository {

    private val api: ApiService = RetrofitClient.api

    suspend fun login(email: String, password: String): Response<LoginResponse> {
        val request = LoginRequest(email, password)
        return api.login(request)
    }
}