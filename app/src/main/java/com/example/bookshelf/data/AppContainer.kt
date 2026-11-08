package com.example.bookshelf.data

import com.example.bookshelf.network.BookshelfApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.bookshelf.BuildConfig

interface AppContainer {
    val booksRepository: BooksRepository
}

class DefaultAppContainer: AppContainer {
    private val baseUrl = "https://www.googleapis.com/books/v1/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseUrl)
        .build()

    private val retrofitService: BookshelfApiService by lazy {
        retrofit.create(BookshelfApiService::class.java)
    }

    override val booksRepository: BooksRepository by lazy {
        NetworkBooksRepository(
            bookshelfApiService = retrofitService,
            apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY,
            packageName = BuildConfig.APPLICATION_ID,
            certificate = BuildConfig.GOOGLE_BOOKS_CERTIFICATE
        )
    }
}