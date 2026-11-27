
package com.example.foundit.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foundit.data.local.FoundItemDatabase
import com.example.foundit.data.local.ChatMessageEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val dao =
        FoundItemDatabase.getDatabase(application).communityDao()

    private val _messages =
        MutableStateFlow<List<ChatMessageEntity>>(emptyList())

    val messages: StateFlow<List<ChatMessageEntity>> =
        _messages.asStateFlow()

    private var conversationJob: Job? = null


    fun loadConversation(
        itemId: Int,
        currentUser: String,
        otherUser: String
    ) {
        viewModelScope.launch {
            dao.messages(itemId, currentUser, otherUser).collect {
                _messages.value = it
            }
        }
    }


    fun sendMessage(
        itemId: Int,
        sender: String,
        receiver: String,
        body: String
    ) {
        viewModelScope.launch {
            val clean = body.trim()

            if (clean.isEmpty()) return@launch

            if (sender.isBlank() || receiver.isBlank()) {
                Log.e(
                    "FoundItChat",
                    "Cannot send: sender or receiver is empty"
                )
                return@launch
            }

            try {
                dao.send(
                    ChatMessageEntity(
                        itemId = itemId,
                        senderEmail = sender.trim(),
                        receiverEmail = receiver.trim(),
                        body = clean
                    )
                )

                Log.d("FoundItChat", "Message saved successfully")

            } catch (e: Exception) {
                Log.e("FoundItChat", "Error saving message", e)
            }
        }
    }
}
