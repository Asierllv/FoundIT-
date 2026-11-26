package com.example.foundit.data.repository

import com.example.foundit.data.local.FoundItemDao
import com.example.foundit.data.local.FoundItemEntity
import com.example.foundit.data.remote.FoundItemRemoteRepository
import kotlinx.coroutines.flow.Flow

/** Room is the local source of truth. Remote sample posts are fetched separately and never overwrite user listings. */
class ExploreRepository(
    private val dao: FoundItemDao,
    private val remote: FoundItemRemoteRepository = FoundItemRemoteRepository()
) {
    fun observeLocalListings(): Flow<List<FoundItemEntity>> = dao.getAllItems()
    suspend fun refreshRemoteSample() = remote.getItems()
    suspend fun createRemoteSample(title: String, location: String, category: String, description: String) =
        remote.createItem(title, location, category, description)
}
