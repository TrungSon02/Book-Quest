package com.example.bookquest.ui.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookquest.ui.theme.BookQuestTheme

@Composable
fun ReadingScreen(
    book: ReadingBook,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var chapterIndex by rememberSaveable { mutableIntStateOf(0) }
    var pageIndex by rememberSaveable { mutableIntStateOf(0) }

    val chapter = book.chapters[chapterIndex]
    val totalPages = chapter.pages.size

    Scaffold(
        modifier = modifier,
        bottomBar = {
            PageNavigationBar(
                currentPage = pageIndex + 1,
                totalPages = totalPages,
                onPrevious = { if (pageIndex > 0) pageIndex-- },
                onNext = { if (pageIndex < totalPages - 1) pageIndex++ }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            CoverHeader(onBackClick = onBackClick)

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ChapterSelector(
                    chapters = book.chapters,
                    selectedIndex = chapterIndex,
                    onSelect = {
                        chapterIndex = it
                        pageIndex = 0
                    }
                )

                Text(
                    text = chapter.pages[pageIndex],
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 28.sp
                )
            }
        }
    }
}

@Composable
private fun CoverHeader(onBackClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        FilledTonalIconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to home")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterSelector(
    chapters: List<ReadingChapter>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = chapters[selectedIndex].title,
            onValueChange = {},
            readOnly = true,
            label = { Text("Chapter") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            chapters.forEachIndexed { index, chapter ->
                DropdownMenuItem(
                    text = { Text(chapter.title) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PageNavigationBar(
    currentPage: Int,
    totalPages: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious, enabled = currentPage > 1) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous page")
            }
            Text(
                text = "$currentPage / $totalPages",
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onNext, enabled = currentPage < totalPages) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next page")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReadingScreenPreview() {
    BookQuestTheme {
        ReadingScreen(
            book = ReadingBook(
                title = "Sample Book",
                author = "Author Name",
                chapters = listOf(
                    ReadingChapter(
                        "Chapter 1",
                        listOf("The morning fog drifted slowly over the quiet harbor, hiding the fishing boats until only their masts rose above the mist. Mara stood at the end of the old wooden pier, holding a thermos of tea and watching the water move beneath her feet. She had come here every day for a month, ever since her grandfather had left her the small bookshop on the corner of Willow Street. At first, the shop had felt like a burden, a room full of dusty shelves and stories nobody wanted to read. But slowly, page by page, she had begun to understand why he had loved it so much.\n\nEach book carried a small piece of the person who had owned it before. A faded ticket stub marked the middle of a mystery novel. A pressed flower rested between the pages of a poetry collection. In the margins of a worn travel guide, someone had written tiny notes about restaurants, train times, and the names of strangers who had been kind. Mara read them all, one evening after another, and the shop no longer felt empty. It felt like a library of lives, patiently waiting for someone to listen.\n\nAs the sun climbed higher and the fog began to lift, she finished her tea and walked back toward the shop. A young boy was already waiting outside the door, pressing his nose against the glass. He turned and asked if the store was open yet. Mara smiled, found the heavy brass key in her pocket, and told him that it was now.",
                            "Page two text.",
                            "Page three text.")
                    ),
                    ReadingChapter(
                        "Chapter 2",
                        listOf("Chapter 2 page one.", "Chapter 2 page two.")
                    )
                )
            ),
            onBackClick = {}
        )
    }
}