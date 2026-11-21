package com.example.bookquest.ui.reading

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bookquest.R
import com.example.bookquest.ui.theme.BookQuestTheme
import data.AppDatabase
import kotlin.math.roundToInt

private enum class ActiveDialog { None, PageQuestion, ChapterQuiz }

private enum class AfterPageQuestion { NextPage, ChapterQuiz }

private const val MIN_ZOOM_SCALE = 0.70f
private const val MAX_ZOOM_SCALE = 2.20f
private const val ZOOM_BUTTON_STEP = 0.10f
private const val BASE_FONT_SIZE_SP = 18f
private const val BASE_LINE_HEIGHT_SP = 27f

internal data class DynamicPageSlice(
    val text: String,
    val startCharOffset: Int,
    val endCharOffset: Int,
    val wordCount: Int
)

/**
 * Slices a laid-out chapter text into exact 1-screen pages that fit within [maxHeightPx]
 * so pages behave like a Kindle reader and dynamically re-paginate when zoomed in/out.
 */
internal fun sliceLinesIntoPages(
    chapterText: String,
    maxHeightPx: Int,
    lineCount: Int,
    getLineTop: (Int) -> Float,
    getLineBottom: (Int) -> Float,
    getLineStart: (Int) -> Int,
    getLineEnd: (Int) -> Int
): List<DynamicPageSlice> {
    if (chapterText.isBlank() || lineCount <= 0 || maxHeightPx <= 0) {
        val trimmed = chapterText.trim()
        return listOf(
            DynamicPageSlice(
                text = trimmed,
                startCharOffset = 0,
                endCharOffset = chapterText.length,
                wordCount = countWords(trimmed)
            )
        )
    }

    fun isLineBlank(lineIdx: Int): Boolean {
        val s = getLineStart(lineIdx).coerceIn(0, chapterText.length)
        val e = getLineEnd(lineIdx).coerceIn(s, chapterText.length)
        return chapterText.substring(s, e).isBlank()
    }

    val pages = mutableListOf<DynamicPageSlice>()
    var startLine = 0

    while (startLine < lineCount) {
        while (startLine < lineCount && isLineBlank(startLine)) {
            startLine++
        }
        if (startLine >= lineCount) break

        val pageTop = getLineTop(startLine)
        var endLine = startLine
        while (endLine + 1 < lineCount) {
            val nextBottom = getLineBottom(endLine + 1)
            if (nextBottom - pageTop <= maxHeightPx) {
                endLine++
            } else {
                break
            }
        }

        val nextStartLine = endLine + 1
        var trimmedEndLine = endLine
        while (trimmedEndLine > startLine && isLineBlank(trimmedEndLine)) {
            trimmedEndLine--
        }

        val startOffset = getLineStart(startLine).coerceIn(0, chapterText.length)
        val endOffset = getLineEnd(trimmedEndLine).coerceIn(startOffset, chapterText.length)
        val sliceText = chapterText.substring(startOffset, endOffset).trimEnd()

        if (sliceText.isNotBlank()) {
            pages += DynamicPageSlice(
                text = sliceText,
                startCharOffset = startOffset,
                endCharOffset = endOffset,
                wordCount = countWords(sliceText)
            )
        }

        startLine = nextStartLine
    }

    if (pages.isEmpty()) {
        val trimmed = chapterText.trim()
        pages += DynamicPageSlice(
            text = trimmed,
            startCharOffset = 0,
            endCharOffset = chapterText.length,
            wordCount = countWords(trimmed)
        )
    }
    return pages
}

private fun countWords(text: String): Int {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return 0
    return trimmed.split(Regex("\\s+")).size
}

/**
 * Loads the selected book's EPUB text from `assets/book_data/` and displays [ReadingScreen].
 */
@Composable
fun ReadingBookRoute(
    bookId: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)

    val context = LocalContext.current
    val bookDao = remember { AppDatabase.get(context).bookDao() }

    val loadedBook by produceState<ReadingBook?>(initialValue = null, key1 = bookId) {
        val entity = bookDao.getBookById(bookId)
        value = if (entity != null) {
            val epubUrl = bookDao.getEpubFileUrl(bookId)
            val dbChapters = bookDao.getChaptersForBook(bookId)
            EpubBookLoader.loadFromAssets(
                context = context,
                book = entity,
                epubAssetPath = epubUrl,
                dbChapters = dbChapters
            )
        } else {
            ReadingBook(
                title = "Book Not Found",
                author = "",
                chapters = listOf(
                    ReadingChapter(
                        title = "Chapter 1",
                        pages = listOf("Unable to load book content.")
                    )
                )
            )
        }
    }

    val currentBook = loadedBook
    if (currentBook == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Loading EPUB chapters...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        ReadingScreen(
            book = currentBook,
            onBackClick = onBackClick,
            modifier = modifier
        )
    }
}

@Composable
fun ReadingScreen(
    book: ReadingBook,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var chapterIndex by rememberSaveable { mutableIntStateOf(0) }
    var pageIndex by rememberSaveable { mutableIntStateOf(0) }
    var anchorCharOffset by rememberSaveable { mutableIntStateOf(0) }
    var rawZoomScale by rememberSaveable { mutableFloatStateOf(1f) }
    var activeDialog by rememberSaveable { mutableStateOf(ActiveDialog.None) }
    var afterPageQuestion by rememberSaveable { mutableStateOf(AfterPageQuestion.NextPage) }
    val pageResults = remember { mutableStateMapOf<String, Boolean>() }

    // Quantize zoom to 5% steps so pinch-to-zoom reflows pages smoothly without frame drops
    val zoomScale = ((rawZoomScale * 20f).roundToInt() / 20f).coerceIn(MIN_ZOOM_SCALE, MAX_ZOOM_SCALE)

    val safeChapterIndex = chapterIndex.coerceIn(0, (book.chapters.size - 1).coerceAtLeast(0))
    val chapter = book.chapters.getOrElse(safeChapterIndex) {
        ReadingChapter(title = "Chapter 1", pages = listOf(""))
    }

    val fullChapterText = remember(safeChapterIndex, chapter.pages) {
        chapter.pages.joinToString("\n\n").trim()
    }

    val readingTextStyle = MaterialTheme.typography.bodyLarge.copy(
        fontSize = (BASE_FONT_SIZE_SP * zoomScale).sp,
        lineHeight = (BASE_LINE_HEIGHT_SP * zoomScale).sp
    )

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    var dynamicPages by remember(safeChapterIndex) {
        mutableStateOf(
            listOf(
                DynamicPageSlice(
                    text = chapter.pages.firstOrNull().orEmpty(),
                    startCharOffset = 0,
                    endCharOffset = chapter.pages.firstOrNull()?.length ?: 0,
                    wordCount = countWords(chapter.pages.firstOrNull().orEmpty())
                )
            )
        )
    }

    val totalPages = dynamicPages.size.coerceAtLeast(1)
    val safePageIndex = pageIndex.coerceIn(0, totalPages - 1)
    val currentSlice = dynamicPages.getOrElse(safePageIndex) {
        DynamicPageSlice("", 0, 0, 0)
    }

    val isLastPage = safePageIndex == totalPages - 1
    val pageKey = "$safeChapterIndex:$safePageIndex"
    val pageQuestion = chapter.pageQuestions.getOrNull(safePageIndex)
    val pageQuestionPending = pageQuestion != null && !pageResults.containsKey(pageKey)
    val hasChapterQuiz = chapter.questions.isNotEmpty()

    fun navigateToPage(newIndex: Int) {
        val clamped = newIndex.coerceIn(0, (dynamicPages.size - 1).coerceAtLeast(0))
        pageIndex = clamped
        anchorCharOffset = dynamicPages.getOrNull(clamped)?.startCharOffset ?: 0
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            PageNavigationBar(
                currentPage = safePageIndex + 1,
                totalPages = totalPages,
                wordCount = currentSlice.wordCount,
                onPrevious = {
                    if (safePageIndex > 0) navigateToPage(safePageIndex - 1)
                },
                onNext = {
                    if (pageQuestionPending) {
                        afterPageQuestion = AfterPageQuestion.NextPage
                        activeDialog = ActiveDialog.PageQuestion
                    } else if (safePageIndex < totalPages - 1) {
                        navigateToPage(safePageIndex + 1)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReaderTopBar(
                book = book,
                zoomScale = zoomScale,
                onBackClick = onBackClick,
                onZoomOut = {
                    rawZoomScale = (zoomScale - ZOOM_BUTTON_STEP).coerceAtLeast(MIN_ZOOM_SCALE)
                },
                onZoomReset = {
                    rawZoomScale = 1f
                },
                onZoomIn = {
                    rawZoomScale = (zoomScale + ZOOM_BUTTON_STEP).coerceAtMost(MAX_ZOOM_SCALE)
                }
            )

            ChapterSelector(
                chapters = book.chapters,
                selectedIndex = safeChapterIndex,
                onSelect = {
                    chapterIndex = it
                    pageIndex = 0
                    anchorCharOffset = 0
                }
            )

            // Kindle-style fixed 1-screen page viewport
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pinchToZoom { factor ->
                        rawZoomScale = (rawZoomScale * factor).coerceIn(MIN_ZOOM_SCALE, MAX_ZOOM_SCALE)
                    }
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    val pageWidthPx = with(density) { maxWidth.roundToPx() }.coerceAtLeast(1)
                    val pageHeightPx = with(density) { (maxHeight - 4.dp).roundToPx() }.coerceAtLeast(1)

                    val measuredSlices = remember(
                        fullChapterText,
                        pageWidthPx,
                        pageHeightPx,
                        zoomScale
                    ) {
                        val layoutResult = textMeasurer.measure(
                            text = AnnotatedString(fullChapterText),
                            style = readingTextStyle,
                            constraints = Constraints(maxWidth = pageWidthPx)
                        )
                        sliceLinesIntoPages(
                            chapterText = fullChapterText,
                            maxHeightPx = pageHeightPx,
                            lineCount = layoutResult.lineCount,
                            getLineTop = layoutResult::getLineTop,
                            getLineBottom = layoutResult::getLineBottom,
                            getLineStart = layoutResult::getLineStart,
                            getLineEnd = { line -> layoutResult.getLineEnd(line, visibleEnd = false) }
                        )
                    }

                    LaunchedEffect(measuredSlices) {
                        dynamicPages = measuredSlices
                        val matchedIdx = measuredSlices.indexOfLast { slice ->
                            slice.startCharOffset <= anchorCharOffset
                        }.coerceAtLeast(0)
                        pageIndex = matchedIdx
                    }

                    val activeSlice = measuredSlices.getOrElse(
                        pageIndex.coerceIn(0, (measuredSlices.size - 1).coerceAtLeast(0))
                    ) {
                        DynamicPageSlice("", 0, 0, 0)
                    }

                    Text(
                        text = activeSlice.text,
                        style = readingTextStyle,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

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
                                if (safePageIndex < totalPages - 1) navigateToPage(safePageIndex + 1)
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

/**
 * Recognizes 2-finger pinch zoom gestures on the reading page.
 */
private fun Modifier.pinchToZoom(onZoomChange: (Float) -> Unit): Modifier =
    pointerInput(Unit) {
        awaitEachGesture {
            do {
                val event = awaitPointerEvent()
                if (event.changes.size >= 2) {
                    val zoom = event.calculateZoom()
                    if (zoom != 1f && zoom > 0f) {
                        onZoomChange(zoom)
                        event.changes.forEach { it.consume() }
                    }
                }
            } while (event.changes.any { it.pressed })
        }
    }

@Composable
private fun ReaderTopBar(
    book: ReadingBook,
    zoomScale: Float,
    onBackClick: () -> Unit,
    onZoomOut: () -> Unit,
    onZoomReset: () -> Unit,
    onZoomIn: () -> Unit
) {
    val zoomPercent = (zoomScale * 100).roundToInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilledTonalIconButton(
            onClick = onBackClick,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to home")
        }

        if (!book.coverImage.isNullOrBlank()) {
            AsyncImage(
                model = "file:///android_asset/${book.coverImage}",
                contentDescription = book.title,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.sample_cover),
                error = painterResource(R.drawable.sample_cover),
                modifier = Modifier
                    .size(width = 34.dp, height = 50.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onZoomOut,
                enabled = zoomScale > MIN_ZOOM_SCALE + 0.01f,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("A-", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            TextButton(
                onClick = onZoomReset,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("$zoomPercent%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(
                onClick = onZoomIn,
                enabled = zoomScale < MAX_ZOOM_SCALE - 0.01f,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("A+", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
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
            value = chapters.getOrNull(selectedIndex)?.title ?: "Chapter",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
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
    wordCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious, enabled = currentPage > 1) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous page")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Page $currentPage / $totalPages",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$wordCount words on page",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                        )
                    )
                )
            ),
            onBackClick = {}
        )
    }
}
