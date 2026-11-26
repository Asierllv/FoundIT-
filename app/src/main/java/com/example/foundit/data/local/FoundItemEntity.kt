package com.example.foundit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.foundit.FoundItem

@Entity(tableName = "found_items")
data class FoundItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val location: String,
    val category: String,
    val date: String,
    val description: String,
    val finderContact: String,
    val photoUri: String? = null,
    val isFavorite: Boolean = false,
    val ownerEmail: String = "",
    val status: String = "FOUND"
)

fun FoundItemEntity.toFoundItem() = FoundItem(
    id = id, title = title, location = location, category = category, date = date,
    description = description, finderContact = finderContact, photoUri = photoUri,
    isFavorite = isFavorite, ownerEmail = ownerEmail, status = status
)

fun FoundItem.toEntity() = FoundItemEntity(
    id = 0, title = title, location = location, category = category, date = date,
    description = description, finderContact = finderContact, photoUri = photoUri,
    isFavorite = isFavorite, ownerEmail = ownerEmail, status = status
)
