package com.tomydp.finance_game_moblile.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomydp.finance_game_moblile.network.ProfileResponse
import com.tomydp.finance_game_moblile.repository.AuthRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _profile = MutableLiveData<ProfileResponse>()
    val profile: LiveData<ProfileResponse> = _profile

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchProfile(token: String) {
        viewModelScope.launch {
            try {
                val response = authRepository.getProfile(token)
                if (response.isSuccessful) {
                    _profile.value = response.body()
                } else {
                    _error.value = "Error fetching profile: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error fetching profile: ${e.message}"
            }
        }
    }
}
