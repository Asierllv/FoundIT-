package com.example.foundit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoundItemDao {
    @Query("SELECT * FROM found_items ORDER BY id DESC")
    fun getAllItems(): Flow<List<FoundItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: FoundItemEntity)

    @Query("UPDATE found_items SET isFavorite = :favorite WHERE id = :itemId")
    suspend fun setFavorite(itemId: Int, favorite: Boolean)

    @Query("DELETE FROM found_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: Int)

    @Query("DELETE FROM found_items")
    suspend fun deleteAllItems()
}
