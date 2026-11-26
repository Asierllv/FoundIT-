package com.example.foundit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foundit.data.local.FoundItemDatabase
import com.example.foundit.data.local.FoundItemEntity
import com.example.foundit.data.repository.ExploreRepository
import com.example.foundit.data.remote.FoundItemDto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface RemoteFeedState {
    data object Idle : RemoteFeedState
    data object Loading : RemoteFeedState
    data class Success(val posts: List<FoundItemDto>) : RemoteFeedState
    data object Empty : RemoteFeedState
    data class Error(val message: String) : RemoteFeedState
}

class ExploreViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ExploreRepository(FoundItemDatabase.getDatabase(application).foundItemDao())
    val localListings: StateFlow<List<FoundItemEntity>> = repository.observeLocalListings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _remoteState = kotlinx.coroutines.flow.MutableStateFlow<RemoteFeedState>(RemoteFeedState.Idle)
    val remoteState: StateFlow<RemoteFeedState> = _remoteState

    fun refreshRemoteSample() = viewModelScope.launch {
        _remoteState.value = RemoteFeedState.Loading
        try {
            val result = repository.refreshRemoteSample()
            _remoteState.value = if (result.isEmpty()) RemoteFeedState.Empty else RemoteFeedState.Success(result)
        } catch (e: Exception) {
            _remoteState.value = RemoteFeedState.Error(e.message ?: "Unable to load remote sample feed")
        }
    }

    fun createRemoteSample(title: String, location: String, category: String, description: String) = viewModelScope.launch {
        _remoteState.value = RemoteFeedState.Loading
        try {
            _remoteState.value = RemoteFeedState.Success(listOf(repository.createRemoteSample(title, location, category, description)))
        } catch (e: Exception) {
            _remoteState.value = RemoteFeedState.Error(e.message ?: "Unable to submit sample")
        }
    }
}
