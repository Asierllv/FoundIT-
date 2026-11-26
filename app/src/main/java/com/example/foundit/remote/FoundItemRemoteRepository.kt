package com.example.foundit.data.remote

class FoundItemRemoteRepository(
    private val api: FoundItemApi = RetrofitClient.api
) {

    suspend fun getItems(): List<FoundItemDto> {
        return api.getItems()
    }

    suspend fun createItem(
        title: String,
        location: String,
        category: String,
        description: String
    ): FoundItemDto {

        val request = FoundItemDto(
            userId = 1,
            title = title,
            body = """
                Category: $category
                Location: $location
                Description: $description
            """.trimIndent()
        )

        val response = api.createItem(request)

        if (!response.isSuccessful) {
            throw Exception(
                "API error: ${response.code()}"
            )
        }

        return response.body()
            ?: throw Exception("Empty API response")
    }
}