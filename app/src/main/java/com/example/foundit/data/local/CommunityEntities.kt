package com.example.foundit.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages", indices = [Index(value = ["itemId", "createdAt"])])
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Int,
    val senderEmail: String,
    val receiverEmail: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0") val isRead: Boolean = false
)

@Entity(tableName = "reviews", indices = [Index(value = ["reviewedEmail", "createdAt"])])
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Int,
    val reviewerEmail: String,
    val reviewedEmail: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)
