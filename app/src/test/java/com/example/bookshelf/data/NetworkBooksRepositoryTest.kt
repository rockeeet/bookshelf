package com.example.bookshelf.data

import com.example.bookshelf.model.Book
import com.example.bookshelf.model.BookDetailsResponse
import com.example.bookshelf.model.BookSearchItem
import com.example.bookshelf.model.BookSearchResponse
import com.example.bookshelf.model.ImageLinks
import com.example.bookshelf.model.VolumeInfo
import com.example.bookshelf.network.BookshelfApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkBooksRepositoryTest {

    @Test
    fun getBooks_returnsCorrectBook() = runBlocking {

        // Arrange — подготавливаем тестовые данные

        val apiService = mockk<BookshelfApiService>()

        coEvery {
            apiService.searchBooks(any(), any(), any(), any())
        } returns BookSearchResponse(
            items = listOf(
                BookSearchItem(id = "book123")
            )
        )

        coEvery {
            apiService.getBookDetails(any(), any(), any(), any())
        } returns BookDetailsResponse(
            volumeInfo = VolumeInfo(
                title = "Clean Code",
                authors = listOf("Robert C. Martin"),
                description = "A handbook of agile software craftsmanship.",
                publishedDate = "2008",
                publisher = "Prentice Hall",
                averageRating = 4.5,
                ratingsCount = 100,
                categories = listOf("Computers"),
                imageLinks = ImageLinks(
                    thumbnail = "http://example.com/book.jpg"
                )
            )
        )

        val repository = NetworkBooksRepository(
            bookshelfApiService = apiService,
            apiKey = "test-api-key",
            packageName = "com.example.bookshelf",
            certificate = "test-certificate"
        )

        // Act — выполняем действие, которое тестируем

        val result = repository.getBooks("clean code")

        // Assert — проверяем результат

        val expectedBook = Book(
            id = "book123",
            title = "Clean Code",
            authors = listOf("Robert C. Martin"),
            description = "A handbook of agile software craftsmanship.",
            publishedDate = "2008",
            publisher = "Prentice Hall",
            averageRating = 4.5,
            ratingsCount = 100,
            categories = listOf("Computers"),
            imageUrl = "https://example.com/book.jpg"
        )

        assertEquals(listOf(expectedBook), result)

        coVerify(exactly = 1) {
            apiService.searchBooks(
                "clean code",
                "test-api-key",
                "com.example.bookshelf",
                "test-certificate"
            )
        }

        coVerify(exactly = 1) {
            apiService.getBookDetails(
                "book123",
                "test-api-key",
                "com.example.bookshelf",
                "test-certificate"
            )
        }
    }

    @Test
    fun getBooks_returnsEmptyListWhenNoBooksFound() = runBlocking {

        val apiService = mockk<BookshelfApiService>()

        coEvery {
            apiService.searchBooks(any(), any(), any(), any())
        } returns BookSearchResponse(
            items = emptyList()
        )

        val repository = NetworkBooksRepository(
            bookshelfApiService = apiService,
            apiKey = "test-api-key",
            packageName = "com.example.bookshelf",
            certificate = "test-certificate"
        )

        val result = repository.getBooks("unknown book")

        assertEquals(emptyList<Book>(), result)

        coVerify(exactly = 0) {
            apiService.getBookDetails(any(), any(), any(), any())
        }
    }

    @Test
    fun getBooks_skipsBookWhenVolumeInfoIsNull() = runBlocking {

        val apiService = mockk<BookshelfApiService>()

        coEvery {
            apiService.searchBooks(any(), any(), any(), any())
        } returns BookSearchResponse(
            items = listOf(
                BookSearchItem(id = "book123")
            )
        )

        coEvery {
            apiService.getBookDetails(any(), any(), any(), any())
        } returns BookDetailsResponse(
            volumeInfo = null
        )

        val repository = NetworkBooksRepository(
            bookshelfApiService = apiService,
            apiKey = "test-api-key",
            packageName = "com.example.bookshelf",
            certificate = "test-certificate"
        )

        val result = repository.getBooks("clean code")

        assertEquals(emptyList<Book>(), result)

        coVerify(exactly = 1) {
            apiService.getBookDetails(any(), any(), any(), any())
        }
    }

    @Test
    fun getBooks_skipsBookWhenTitleIsNull() = runBlocking {

        val apiService = mockk<BookshelfApiService>()

        coEvery {
            apiService.searchBooks(any(), any(), any(), any())
        } returns BookSearchResponse(
            items = listOf(BookSearchItem(id = "book123"))
        )

        coEvery {
            apiService.getBookDetails(any(), any(), any(), any())
        } returns BookDetailsResponse(
            volumeInfo = VolumeInfo(
                title = null,
                authors = null,
                description = null,
                publishedDate = null,
                publisher = null,
                averageRating = null,
                ratingsCount = null,
                categories = null,
                imageLinks = ImageLinks("https://example.com/book.jpg")
            )
        )

        val repository = NetworkBooksRepository(
            bookshelfApiService = apiService,
            apiKey = "test-api-key",
            packageName = "com.example.bookshelf",
            certificate = "test-certificate"
        )

        val result = repository.getBooks("clean code")

        assertEquals(emptyList<Book>(), result)

        coVerify(exactly = 1) {
            apiService.getBookDetails(any(), any(), any(), any())
        }
    }

    @Test
    fun getBooks_skipsBookWhenThumbnailIsNull() = runBlocking {

        val apiService = mockk<BookshelfApiService>()

        coEvery {
            apiService.searchBooks(any(), any(), any(), any())
        } returns BookSearchResponse(
            items = listOf(BookSearchItem(id = "book123"))
        )

        coEvery {
            apiService.getBookDetails(any(), any(), any(), any())
        } returns BookDetailsResponse(
            volumeInfo = VolumeInfo(
                title = "Clean Code",
                authors = listOf("Robert C. Martin"),
                description = null,
                publishedDate = null,
                publisher = null,
                averageRating = null,
                ratingsCount = null,
                categories = null,
                imageLinks = ImageLinks(thumbnail = null)
            )
        )

        val repository = NetworkBooksRepository(
            bookshelfApiService = apiService,
            apiKey = "test-api-key",
            packageName = "com.example.bookshelf",
            certificate = "test-certificate"
        )

        val result = repository.getBooks("clean code")

        assertEquals(emptyList<Book>(), result)

        coVerify(exactly = 1) {
            apiService.getBookDetails(any(), any(), any(), any())
        }
    }

    @Test
    fun getBooks_returnsBookWithEmptyAuthorsAndCategories() = runBlocking {

        val apiService = mockk<BookshelfApiService>()

        coEvery {
            apiService.searchBooks(any(), any(), any(), any())
        } returns BookSearchResponse(
            items = listOf(BookSearchItem(id = "book123"))
        )

        coEvery {
            apiService.getBookDetails(any(), any(), any(), any())
        } returns BookDetailsResponse(
            volumeInfo = VolumeInfo(
                title = "Clean Code",
                authors = null,
                description = null,
                publishedDate = null,
                publisher = null,
                averageRating = null,
                ratingsCount = null,
                categories = null,
                imageLinks = ImageLinks(
                    thumbnail = "https://example.com/book.jpg"
                )
            )
        )

        val repository = NetworkBooksRepository(
            bookshelfApiService = apiService,
            apiKey = "test-api-key",
            packageName = "com.example.bookshelf",
            certificate = "test-certificate"
        )

        val result = repository.getBooks("clean code")

        val expectedBook = Book(
            id = "book123",
            title = "Clean Code",
            authors = emptyList(),
            description = null,
            publishedDate = null,
            publisher = null,
            averageRating = null,
            ratingsCount = null,
            categories = emptyList(),
            imageUrl = "https://example.com/book.jpg"
        )

        assertEquals(listOf(expectedBook), result)
    }

    @Test
    fun getBooks_returnsAllBooks() = runBlocking {

        val apiService = mockk<BookshelfApiService>()

        coEvery {
            apiService.searchBooks(any(), any(), any(), any())
        } returns BookSearchResponse(
            items = listOf(
                BookSearchItem(id = "book1"),
                BookSearchItem(id = "book2")
            )
        )

        coEvery {
            apiService.getBookDetails(any(), any(), any(), any())
        } returnsMany listOf(
            BookDetailsResponse(
                volumeInfo = VolumeInfo(
                    title = "Clean Code",
                    authors = listOf("Robert C. Martin"),
                    description = null,
                    publishedDate = "2008",
                    publisher = null,
                    averageRating = null,
                    ratingsCount = null,
                    categories = null,
                    imageLinks = ImageLinks("https://example.com/1.jpg")
                )
            ),
            BookDetailsResponse(
                volumeInfo = VolumeInfo(
                    title = "Kotlin in Action",
                    authors = listOf("Dmitry Jemerov"),
                    description = null,
                    publishedDate = "2017",
                    publisher = null,
                    averageRating = null,
                    ratingsCount = null,
                    categories = null,
                    imageLinks = ImageLinks("https://example.com/2.jpg")
                )
            )
        )

        val repository = NetworkBooksRepository(
            bookshelfApiService = apiService,
            apiKey = "test-api-key",
            packageName = "com.example.bookshelf",
            certificate = "test-certificate"
        )

        val result = repository.getBooks("programming")

        assertEquals(2, result.size)
        assertEquals("book1", result[0].id)
        assertEquals("Clean Code", result[0].title)
        assertEquals("book2", result[1].id)
        assertEquals("Kotlin in Action", result[1].title)

        coVerify(exactly = 2) {
            apiService.getBookDetails(any(), any(), any(), any())
        }
    }
}