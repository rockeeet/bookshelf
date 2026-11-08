package com.example.bookshelf.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookshelf.BookshelfApplication
import com.example.bookshelf.data.BooksRepository
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


class BookshelfViewModel(
    private val booksRepository: BooksRepository
): ViewModel() {

    var bookshelfUiState: BookshelfUiState by mutableStateOf(BookshelfUiState.Loading)
        private set

    var isSearching by mutableStateOf(false)
        private set

    var hasSearchError by mutableStateOf(false)
        private set

    private var currentQuery = "jazz history"

    init {
        getBooks()
    }

    private fun getBooks() {
        val hasPreviousResults = bookshelfUiState is BookshelfUiState.Success

        if (!hasPreviousResults) {
            bookshelfUiState = BookshelfUiState.Loading
        }

        isSearching = true
        hasSearchError = false

        viewModelScope.launch {
            try {
                val books = booksRepository.getBooks(currentQuery)
                bookshelfUiState = BookshelfUiState.Success(books)
            } catch (e: IOException) {
                Log.e("BookshelfViewModel", "Network error", e)

                if (hasPreviousResults) {
                    hasSearchError = true
                } else {
                    bookshelfUiState = BookshelfUiState.Error
                }
            } catch (e: HttpException) {
                Log.e("BookshelfViewModel", "HTTP error: ${e.code()}", e)

                if (hasPreviousResults) {
                    hasSearchError = true
                } else {
                    bookshelfUiState = BookshelfUiState.Error
                }
            } finally {
                isSearching = false
            }
        }
    }

    fun searchBooks(query: String) {
        val normalizedQuery = query.trim()

        if (normalizedQuery.isBlank()) return
        currentQuery = normalizedQuery
        getBooks()
    }

    fun retry() {
        getBooks()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as BookshelfApplication)
                val booksRepository = application.container.booksRepository
                BookshelfViewModel(booksRepository = booksRepository)
            }
        }
    }
}
