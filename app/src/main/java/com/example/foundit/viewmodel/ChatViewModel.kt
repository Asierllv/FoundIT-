package com.example.foundit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foundit.data.local.FoundItemDatabase
import com.example.foundit.data.local.ChatMessageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = FoundItemDatabase.getDatabase(application).communityDao()
    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages.asStateFlow()
    fun loadConversation(itemId: Int, currentUser: String, otherUser: String) = viewModelScope.launch {
        dao.messages(itemId, currentUser, otherUser).collect { _messages.value = it }
    }
    fun sendMessage(itemId: Int, sender: String, receiver: String, body: String) = viewModelScope.launch {
        val clean = body.trim()
        if (clean.isNotEmpty()) dao.send(ChatMessageEntity(itemId = itemId, senderEmail = sender, receiverEmail = receiver, body = clean))
    }
}
