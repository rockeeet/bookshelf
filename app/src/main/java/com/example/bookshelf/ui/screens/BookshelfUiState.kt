package com.example.bookshelf.ui.screens

import com.example.bookshelf.model.Book

sealed interface BookshelfUiState {
    data class Success(val books: List<Book>): BookshelfUiState
    data object Error: BookshelfUiState
    data object Loading: BookshelfUiState
}