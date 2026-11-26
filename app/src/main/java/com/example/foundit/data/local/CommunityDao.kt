package com.example.foundit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityDao {
    @Query("SELECT * FROM chat_messages WHERE itemId = :itemId AND ((senderEmail = :userEmail AND receiverEmail = :otherEmail) OR (senderEmail = :otherEmail AND receiverEmail = :userEmail)) ORDER BY createdAt ASC")
    fun messages(itemId: Int, userEmail: String, otherEmail: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE senderEmail = :userEmail OR receiverEmail = :userEmail ORDER BY createdAt DESC")
    fun allMessagesFor(userEmail: String): Flow<List<ChatMessageEntity>>

    @Query("UPDATE chat_messages SET isRead = 1 WHERE itemId = :itemId AND senderEmail = :otherEmail AND receiverEmail = :userEmail")
    suspend fun markConversationRead(itemId: Int, userEmail: String, otherEmail: String)

    @Insert
    suspend fun send(message: ChatMessageEntity)

    @Query("SELECT * FROM reviews WHERE reviewedEmail = :email ORDER BY createdAt DESC")
    fun reviewsFor(email: String): Flow<List<ReviewEntity>>

    @Insert
    suspend fun addReview(review: ReviewEntity)
}
