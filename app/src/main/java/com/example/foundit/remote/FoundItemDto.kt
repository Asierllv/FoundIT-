package com.example.foundit.data.remote

data class FoundItemDto(
    val userId: Int,
    val id: Int? = null,
    val title: String,
    val body: String
)