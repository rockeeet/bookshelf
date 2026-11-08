package com.example.bookshelf

import com.example.bookshelf.data.BooksRepository
import com.example.bookshelf.model.Book
import com.example.bookshelf.ui.screens.BookshelfUiState
import com.example.bookshelf.ui.screens.BookshelfViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class BookshelfViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun bookshelfViewModel_initialLoad_success() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val booksRepository = mockk<BooksRepository>()

        val expectedBooks = listOf(
            Book(
                id = "1",
                title = "Kotlin for Beginners",
                authors = listOf("John Smith"),
                description = "A Kotlin book",
                publishedDate = "2025",
                publisher = "Example Publisher",
                averageRating = 4.5,
                ratingsCount = 100,
                categories = listOf("Programming"),
                imageUrl = "https://example.com/book.jpg"
            )
        )

        coEvery { booksRepository.getBooks("jazz history") } returns expectedBooks

        // Act
        val viewModel = BookshelfViewModel(booksRepository)

        advanceUntilIdle()

        // Assert
        assertEquals(
            BookshelfUiState.Success(expectedBooks),
            viewModel.bookshelfUiState
        )
        assertEquals(false, viewModel.isSearching)
        assertEquals(false, viewModel.hasSearchError)

        coVerify(exactly = 1) {
            booksRepository.getBooks("jazz history")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun bookshelfViewModel_initialLoad_error() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val booksRepository = mockk<BooksRepository>()

        coEvery {
            booksRepository.getBooks("jazz history")
        } throws IOException()

        mockkStatic(Log::class)

        every {
            Log.e(any(), any(), any<Throwable>())
        } returns 0

        // Act
        val viewModel = BookshelfViewModel(booksRepository)

        advanceUntilIdle()

        // Assert
        assertEquals(
            BookshelfUiState.Error,
            viewModel.bookshelfUiState
        )
        assertEquals(false, viewModel.isSearching)
        assertEquals(false, viewModel.hasSearchError)

        unmockkStatic(Log::class)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun bookshelfViewModel_initialLoad_httpError() =
        runTest(mainDispatcherRule.testDispatcher) {

            // Arrange
            val booksRepository = mockk<BooksRepository>()

            val errorResponse = Response.error<Any>(
                500,
                "Server error".toResponseBody("text/plain".toMediaType())
            )

            val exception = HttpException(errorResponse)

            coEvery {
                booksRepository.getBooks("jazz history")
            } throws exception

            mockkStatic(Log::class)

            every {
                Log.e(any(), any(), any<Throwable>())
            } returns 0

            // Act
            val viewModel = BookshelfViewModel(booksRepository)

            advanceUntilIdle()

            // Assert
            assertEquals(
                BookshelfUiState.Error,
                viewModel.bookshelfUiState
            )
            assertEquals(false, viewModel.isSearching)
            assertEquals(false, viewModel.hasSearchError)

            coVerify(exactly = 1) {
                booksRepository.getBooks("jazz history")
            }

            unmockkStatic(Log::class)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun bookshelfViewModel_searchError_keepsPreviousResults() =
        runTest(mainDispatcherRule.testDispatcher) {

            // Arrange
            val booksRepository = mockk<BooksRepository>()

            val expectedBooks = listOf(
                Book(
                    id = "1",
                    title = "Kotlin for Beginners",
                    authors = listOf("John Smith"),
                    description = "A Kotlin book",
                    publishedDate = "2025",
                    publisher = "Example Publisher",
                    averageRating = 4.5,
                    ratingsCount = 100,
                    categories = listOf("Programming"),
                    imageUrl = "https://example.com/book.jpg"
                )
            )

            // Первый запрос успешный
            coEvery {
                booksRepository.getBooks("jazz history")
            } returns expectedBooks

            // Второй запрос завершается ошибкой
            coEvery {
                booksRepository.getBooks("rock history")
            } throws IOException()

            mockkStatic(Log::class)

            every {
                Log.e(any(), any(), any<Throwable>())
            } returns 0

            // Act
            val viewModel = BookshelfViewModel(booksRepository)

            advanceUntilIdle()

            viewModel.searchBooks("rock history")

            advanceUntilIdle()

            // Assert
            assertEquals(
                BookshelfUiState.Success(expectedBooks),
                viewModel.bookshelfUiState
            )
            assertEquals(false, viewModel.isSearching)
            assertEquals(true, viewModel.hasSearchError)

            coVerify(exactly = 1) {
                booksRepository.getBooks("jazz history")
            }

            coVerify(exactly = 1) {
                booksRepository.getBooks("rock history")
            }

            unmockkStatic(Log::class)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun bookshelfViewModel_searchSuccess_updatesBooks() =
        runTest(mainDispatcherRule.testDispatcher) {

            // Arrange
            val booksRepository = mockk<BooksRepository>()

            val initialBooks = listOf(
                Book(
                    id = "1",
                    title = "Kotlin for Beginners",
                    authors = listOf("John Smith"),
                    description = "A Kotlin book",
                    publishedDate = "2025",
                    publisher = "Example Publisher",
                    averageRating = 4.5,
                    ratingsCount = 100,
                    categories = listOf("Programming"),
                    imageUrl = "https://example.com/kotlin.jpg"
                )
            )

            val searchedBooks = listOf(
                Book(
                    id = "2",
                    title = "History of Rock",
                    authors = listOf("Jane Doe"),
                    description = "A book about rock music",
                    publishedDate = "2024",
                    publisher = "Music Publisher",
                    averageRating = 4.8,
                    ratingsCount = 200,
                    categories = listOf("Music"),
                    imageUrl = "https://example.com/rock.jpg"
                )
            )

            coEvery {
                booksRepository.getBooks("jazz history")
            } returns initialBooks

            coEvery {
                booksRepository.getBooks("rock history")
            } returns searchedBooks

            // Act
            val viewModel = BookshelfViewModel(booksRepository)

            advanceUntilIdle()

            viewModel.searchBooks("rock history")

            advanceUntilIdle()

            // Assert
            assertEquals(
                BookshelfUiState.Success(searchedBooks),
                viewModel.bookshelfUiState
            )
            assertEquals(false, viewModel.isSearching)
            assertEquals(false, viewModel.hasSearchError)

            coVerify(exactly = 1) {
                booksRepository.getBooks("jazz history")
            }

            coVerify(exactly = 1) {
                booksRepository.getBooks("rock history")
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun bookshelfViewModel_emptySearch_doesNotCallRepository() =
        runTest(mainDispatcherRule.testDispatcher) {

            // Arrange
            val booksRepository = mockk<BooksRepository>()

            coEvery {
                booksRepository.getBooks("jazz history")
            } returns emptyList()

            // Act
            val viewModel = BookshelfViewModel(booksRepository)

            advanceUntilIdle()

            viewModel.searchBooks("   ")

            advanceUntilIdle()

            // Assert
            coVerify(exactly = 0) {
                booksRepository.getBooks("   ")
            }

            assertEquals(
                BookshelfUiState.Success(emptyList()),
                viewModel.bookshelfUiState
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun bookshelfViewModel_retry_successAfterError() =
        runTest(mainDispatcherRule.testDispatcher) {

            // Arrange
            val booksRepository = mockk<BooksRepository>()

            val expectedBooks = listOf(
                Book(
                    id = "1",
                    title = "Kotlin for Beginners",
                    authors = listOf("John Smith"),
                    description = "A Kotlin book",
                    publishedDate = "2025",
                    publisher = "Example Publisher",
                    averageRating = 4.5,
                    ratingsCount = 100,
                    categories = listOf("Programming"),
                    imageUrl = "https://example.com/book.jpg"
                )
            )

            coEvery {
                booksRepository.getBooks("jazz history")
            } throws IOException() andThen expectedBooks

            mockkStatic(Log::class)

            every {
                Log.e(any(), any(), any<Throwable>())
            } returns 0

            // Act
            val viewModel = BookshelfViewModel(booksRepository)

            advanceUntilIdle()

            // Первая загрузка завершилась ошибкой
            assertEquals(
                BookshelfUiState.Error,
                viewModel.bookshelfUiState
            )

            // Повторяем загрузку
            viewModel.retry()

            advanceUntilIdle()

            // Assert
            assertEquals(
                BookshelfUiState.Success(expectedBooks),
                viewModel.bookshelfUiState
            )
            assertEquals(false, viewModel.isSearching)
            assertEquals(false, viewModel.hasSearchError)

            coVerify(exactly = 2) {
                booksRepository.getBooks("jazz history")
            }

            unmockkStatic(Log::class)
        }
}