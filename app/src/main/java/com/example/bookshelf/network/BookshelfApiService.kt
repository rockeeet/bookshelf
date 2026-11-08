package com.example.bookshelf.network

import com.example.bookshelf.model.BookDetailsResponse
import com.example.bookshelf.model.BookSearchResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface BookshelfApiService {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("key") apiKey: String,
        @Header("X-Android-Package") packageName: String,
        @Header("X-Android-Cert") certificate: String
    ): BookSearchResponse

    @GET("volumes/{volumeId}")
    suspend fun getBookDetails(
        @Path("volumeId") volumeId: String,
        @Query("key") apiKey: String,
        @Header("X-Android-Package") packageName: String,
        @Header("X-Android-Cert") certificate: String
    ): BookDetailsResponse
}