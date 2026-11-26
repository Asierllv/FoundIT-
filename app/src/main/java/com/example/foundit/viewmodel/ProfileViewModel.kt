package com.example.foundit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foundit.data.local.FoundItemDatabase
import com.example.foundit.data.local.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = FoundItemDatabase.getDatabase(application).userDao()
    private val _profile = MutableStateFlow<UserEntity?>(null)
    val profile: StateFlow<UserEntity?> = _profile.asStateFlow()
    fun load(email: String) = viewModelScope.launch { _profile.value = dao.findByEmail(email) }
}
