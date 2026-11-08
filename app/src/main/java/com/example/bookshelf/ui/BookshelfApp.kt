package com.example.bookshelf.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bookshelf.R
import com.example.bookshelf.model.Book
import com.example.bookshelf.ui.screens.BookDetailsScreen
import com.example.bookshelf.ui.screens.BookshelfUiState
import com.example.bookshelf.ui.screens.BookshelfViewModel
import com.example.bookshelf.ui.screens.ErrorScreen
import com.example.bookshelf.ui.screens.LoadingScreen
import com.example.bookshelf.ui.screens.MainScreen

@Composable
fun BookshelfApp(
    modifier: Modifier = Modifier,
    viewModel: BookshelfViewModel = viewModel(
        factory = BookshelfViewModel.Factory
    )
) {
    var chosenBook by remember {
        mutableStateOf<Book?>(null)
    }

    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    BackHandler(chosenBook != null) {
        chosenBook = null
    }

    val uiState = viewModel.bookshelfUiState

    Scaffold(
        modifier = modifier,
        topBar = {
            BookshelfTopAppBar(
                book = chosenBook,
                onBackClick = { chosenBook = null }
            )
        }
    ) { innerPadding ->
        when (uiState) {
            is BookshelfUiState.Loading -> {
                LoadingScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            is BookshelfUiState.Success -> {
                if (chosenBook == null) {
                    MainScreen(
                        books = uiState.books,
                        onBookClick = { book ->
                            chosenBook = book
                        },
                        onSearch = viewModel::searchBooks,
                        isSearching = viewModel.isSearching,
                        hasSearchError = viewModel.hasSearchError,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        modifier = Modifier.padding(innerPadding)
                    )
                } else {
                    chosenBook?.let { book ->
                        BookDetailsScreen(
                            book = book,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }

            is BookshelfUiState.Error -> {
                ErrorScreen(
                    retryAction = viewModel::retry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfTopAppBar(
    book: Book?,
    onBackClick: () -> Unit
) {
    if (book == null) {
        CenterAlignedTopAppBar(
            title = {
                Text(stringResource(R.string.app_name))
            }
        )
    } else {
        TopAppBar(
            title = {
                Text(stringResource(R.string.book_details_title))
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(
                            R.string.back_button_description
                        )
                    )
                }
            }
        )
    }
}