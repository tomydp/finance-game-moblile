package com.tomydp.finance_game_moblile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomydp.finance_game_moblile.network.AnalyticsResponse
import com.tomydp.finance_game_moblile.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class AnalyticsState {
    object Loading : AnalyticsState()
    data class Success(val response: AnalyticsResponse) : AnalyticsState()
    data class Error(val message: String) : AnalyticsState()
}

class AnalyticsViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _analyticsState = MutableLiveData<AnalyticsState>()
    val analyticsState: LiveData<AnalyticsState> = _analyticsState

    fun getAnalytics(token: String) {
        _analyticsState.value = AnalyticsState.Loading
        viewModelScope.launch {
            try {
                val response = authRepository.getAnalyticsRankings(token)
                if (response.isSuccessful) {
                    _analyticsState.postValue(AnalyticsState.Success(response.body()!!))
                } else {
                    _analyticsState.postValue(AnalyticsState.Error("Error fetching analytics"))
                }
            } catch (e: Exception) {
                _analyticsState.postValue(AnalyticsState.Error(e.message ?: "Unknown error"))
            }
        }
    }
}
