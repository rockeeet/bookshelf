package com.example.bookshelf.data

import android.util.Log
import com.example.bookshelf.model.Book
import com.example.bookshelf.network.BookshelfApiService

interface BooksRepository {
    suspend fun getBooks(query: String): List<Book>
}

class NetworkBooksRepository(
    private val bookshelfApiService: BookshelfApiService,
    private val apiKey: String,
    private val packageName: String,
    private val certificate: String
): BooksRepository {
    override suspend fun getBooks(query: String): List<Book> {
        val searchResponse = bookshelfApiService.searchBooks(
            query = query,
            apiKey = apiKey,
            packageName = packageName,
            certificate = certificate
        )

        val books = searchResponse.items.orEmpty().mapNotNull { item ->
            val details = bookshelfApiService.getBookDetails(
                volumeId = item.id,
                apiKey = apiKey,
                packageName = packageName,
                certificate = certificate
            )

            val info = details.volumeInfo ?: return@mapNotNull null

            val title = info.title ?: return@mapNotNull null
            val imageUrl = info.imageLinks?.thumbnail
                ?.let { url ->
                    if (url.startsWith("http://")) {
                        url.replaceFirst("http://", "https://")
                    } else {
                        url
                    }
                }
                ?: return@mapNotNull null

            Book(
                id = item.id,
                title = title,
                authors = info.authors.orEmpty(),
                description = info.description,
                publishedDate = info.publishedDate,
                publisher = info.publisher,
                averageRating = info.averageRating,
                ratingsCount = info.ratingsCount,
                categories = info.categories.orEmpty(),
                imageUrl = imageUrl
            )
        }
        return books
    }
}