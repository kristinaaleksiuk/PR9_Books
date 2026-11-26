package com.example.httplab2

data class Author(
    val name: String?,
    val country: String?
)

data class Book(
    val id: Int,
    val title: String,
    val price: Double,
    val inStock: Boolean,
    val author: Author?,
    val genres: List<String>?
)

data class BookResponse(
    val books: List<Book>,
    val total: Int
)