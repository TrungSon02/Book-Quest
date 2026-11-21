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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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

private enum class ActiveDialog { None, PageQuestion, ChapterQuiz }

private enum class AfterPageQuestion { NextPage, ChapterQuiz }

@Composable
fun ReadingScreen(
    book: ReadingBook,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var chapterIndex by rememberSaveable { mutableIntStateOf(0) }
    var pageIndex by rememberSaveable { mutableIntStateOf(0) }
    var activeDialog by rememberSaveable { mutableStateOf(ActiveDialog.None) }
    var afterPageQuestion by rememberSaveable { mutableStateOf(AfterPageQuestion.NextPage) }
    val pageResults = remember { mutableStateMapOf<String, Boolean>() }

    val chapter = book.chapters[chapterIndex]
    val totalPages = chapter.pages.size
    val isLastPage = pageIndex == totalPages - 1
    val pageKey = "$chapterIndex:$pageIndex"
    val pageQuestion = chapter.pageQuestions.getOrNull(pageIndex)
    val pageQuestionPending = pageQuestion != null && !pageResults.containsKey(pageKey)
    val hasChapterQuiz = chapter.questions.isNotEmpty()
    val scrollState = rememberScrollState()

    LaunchedEffect(chapterIndex, pageIndex) {
        scrollState.scrollTo(0)
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            PageNavigationBar(
                currentPage = pageIndex + 1,
                totalPages = totalPages,
                onPrevious = { if (pageIndex > 0) pageIndex-- },
                onNext = {
                    if (pageQuestionPending) {
                        afterPageQuestion = AfterPageQuestion.NextPage
                        activeDialog = ActiveDialog.PageQuestion
                    } else if (pageIndex < totalPages - 1) {
                        pageIndex++
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(scrollState)
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

                if (isLastPage && (pageQuestionPending || hasChapterQuiz)) {
                    Button(
                        onClick = {
                            if (pageQuestionPending) {
                                afterPageQuestion = AfterPageQuestion.ChapterQuiz
                                activeDialog = ActiveDialog.PageQuestion
                            } else {
                                activeDialog = ActiveDialog.ChapterQuiz
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Finish chapter")
                    }
                }
            }
        }
    }

    when (activeDialog) {
        ActiveDialog.PageQuestion -> {
            if (pageQuestion != null) {
                QuestionDialog(
                    questions = listOf(pageQuestion),
                    showSummary = false,
                    onDismiss = { activeDialog = ActiveDialog.None },
                    onAnswered = { pageResults[pageKey] = it },
                    onDone = {
                        activeDialog = ActiveDialog.None
                        when (afterPageQuestion) {
                            AfterPageQuestion.NextPage -> {
                                if (pageIndex < totalPages - 1) pageIndex++
                            }
                            AfterPageQuestion.ChapterQuiz -> {
                                if (hasChapterQuiz) activeDialog = ActiveDialog.ChapterQuiz
                            }
                        }
                    }
                )
            }
        }
        ActiveDialog.ChapterQuiz -> {
            QuestionDialog(
                questions = chapter.questions,
                onDismiss = { activeDialog = ActiveDialog.None }
            )
        }
        ActiveDialog.None -> Unit
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
                        title = "Chapter 1",
                        pages = listOf(
                            "Mara inherited a small bookshop from her grandfather. At first, she thought it was a burden.",
                            "Each old book carried a small piece of its former owner, such as a ticket stub or a pressed flower.",
                            "One morning, a young boy waited outside the shop. Mara smiled and opened the door."
                        ),
                        pageQuestions = listOf(
                            ReadingQuestion(
                                question = "How did Mara feel about the bookshop at first?",
                                options = listOf(
                                    "Excited about it",
                                    "She thought it was a burden",
                                    "She wanted to expand it",
                                    "She had never heard of it"
                                ),
                                correctIndex = 1,
                                explanation = "The text says that at first she thought it was a burden."
                            ),
                            ReadingQuestion(
                                question = "According to the text, what did old books carry?",
                                options = listOf(
                                    "Only dust",
                                    "A small piece of their former owner",
                                    "Maps of the town",
                                    "Secret messages"
                                ),
                                correctIndex = 1,
                                explanation = "Items such as a ticket stub or a pressed flower show traces of previous owners."
                            ),
                            ReadingQuestion(
                                question = "What did Mara do when the boy waited outside?",
                                options = listOf(
                                    "Ignored him",
                                    "Asked him to leave",
                                    "Smiled and opened the door",
                                    "Called her grandfather"
                                ),
                                correctIndex = 2,
                                explanation = "The text says Mara smiled and opened the door."
                            )
                        ),
                        questions = listOf(
                            ReadingQuestion(
                                question = "Why did Mara's feelings about the shop change?",
                                options = listOf(
                                    "She found it was very profitable",
                                    "She began to see the stories inside the old books",
                                    "A friend convinced her to keep it",
                                    "She decided to sell it"
                                ),
                                correctIndex = 1,
                                explanation = "The objects left in the books changed how she saw the shop."
                            ),
                            ReadingQuestion(
                                question = "What does the author suggest about old books?",
                                options = listOf(
                                    "They are worthless",
                                    "They carry small pieces of their former owners",
                                    "They are hard to read",
                                    "They should be thrown away"
                                ),
                                correctIndex = 1,
                                explanation = "The ticket stub and pressed flower suggest the books hold personal traces."
                            )
                        )
                    ),
                    ReadingChapter(
                        title = "Chapter 2",
                        pages = listOf("Chapter 2 page one.", "Chapter 2 page two.")
                    )
                )
            ),
            onBackClick = {}
        )
    }
}