package com.example.bookshelf.model

data class Book(
    val id: String,
    val title: String,
    val authors: List<String>,
    val description: String?,
    val publishedDate: String?,
    val publisher: String?,
    val averageRating: Double?,
    val ratingsCount: Int?,
    val categories: List<String>,
    val imageUrl: String
)
