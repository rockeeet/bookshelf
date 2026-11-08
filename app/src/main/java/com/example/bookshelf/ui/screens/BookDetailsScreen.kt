package com.example.bookshelf.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bookshelf.R
import com.example.bookshelf.model.Book
import com.example.bookshelf.ui.theme.BookshelfTheme

@Composable
fun BookDetailsScreen(
    book: Book,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = book.imageUrl,
            contentDescription = book.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        )

        Column {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = book.authors.joinToString(", ")
                        .ifBlank{ stringResource(R.string.unknown_author) },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(
                        R.string.book_rating,
                        book.averageRating?.toString()
                            ?: stringResource(R.string.no_rating)
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = book.ratingsCount?.let {
                        stringResource(R.string.book_ratings_count, it)
                    } ?: stringResource(R.string.no_rating),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.book_description),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = book.description
                        ?: stringResource(R.string.no_description),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            HorizontalDivider(modifier = Modifier.padding(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.additional_information),
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "${stringResource(R.string.book_publisher)}: ${
                        book.publisher ?: stringResource(R.string.unknown_publisher)
                    }",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "${stringResource(R.string.book_published_date)}: ${
                        book.publishedDate?.take(4)
                            ?: stringResource(R.string.unknown_publication_date)
                    }",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "${stringResource(R.string.book_categories)}: ${
                        book.categories.joinToString(", ")
                            .ifBlank { stringResource(R.string.no_categories) }
                    }",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookDetailsPreview() {
    BookshelfTheme {
        BookDetailsScreen(
            book = Book(
                id = "1",
                title = "The Great Gatsby",
                authors = listOf("F. Scott Fitzgerald"),
                description = "A classic novel about the American Dream.",
                publishedDate = "1925",
                publisher = "Charles Scribner's Sons",
                averageRating = 4.5,
                ratingsCount = 1250,
                categories = listOf("Fiction", "Classics"),
                imageUrl = "https://example.com/book-cover.jpg"
            )
        )
    }
}