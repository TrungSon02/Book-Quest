package com.example.bookquest

import com.example.bookquest.ui.reading.EpubBookLoader
import com.example.bookquest.ui.reading.sliceLinesIntoPages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExampleUnitTest {

    @Test
    fun sliceLinesIntoPages_dynamicallyChangesWordCountWhenZoomChangesLineHeightAndWidth() {
        val words = (1..360).map { "word$it" }
        val chapterText = words.chunked(40).joinToString("\n\n") { it.joinToString(" ") }

        // Simulate layout helper for a given wordsPerLine and lineHeightPx
        fun simulateMeasureAndSlice(wordsPerLine: Int, lineHeightPx: Float, pageHeightPx: Int) = run {
            val lineRanges = mutableListOf<IntRange>()
            var index = 0
            while (index < chapterText.length) {
                if (chapterText[index] == '\n') {
                    lineRanges += index..(index + 1).coerceAtMost(chapterText.length)
                    index++
                    continue
                }
                var wordCount = 0
                var cursor = index
                while (cursor < chapterText.length && chapterText[cursor] != '\n' && wordCount < wordsPerLine) {
                    while (cursor < chapterText.length && chapterText[cursor] == ' ') cursor++
                    if (cursor < chapterText.length && chapterText[cursor] != '\n') {
                        wordCount++
                        while (cursor < chapterText.length && chapterText[cursor] != ' ' && chapterText[cursor] != '\n') {
                            cursor++
                        }
                    }
                }
                while (cursor < chapterText.length && chapterText[cursor] == ' ') cursor++
                lineRanges += index..cursor
                index = cursor
            }

            sliceLinesIntoPages(
                chapterText = chapterText,
                maxHeightPx = pageHeightPx,
                lineCount = lineRanges.size,
                getLineTop = { line -> line * lineHeightPx },
                getLineBottom = { line -> (line + 1) * lineHeightPx },
                getLineStart = { line -> lineRanges[line].first },
                getLineEnd = { line -> lineRanges[line].last }
            )
        }

        // Normal zoom (100%): 10 words/line, 28px line height in 280px viewport
        val normalPages = simulateMeasureAndSlice(wordsPerLine = 10, lineHeightPx = 28f, pageHeightPx = 280)
        // Zoomed in (150%): 6 words/line, 42px line height in 280px viewport
        val zoomedInPages = simulateMeasureAndSlice(wordsPerLine = 6, lineHeightPx = 42f, pageHeightPx = 280)
        // Zoomed out (75%): 13 words/line, 21px line height in 280px viewport
        val zoomedOutPages = simulateMeasureAndSlice(wordsPerLine = 13, lineHeightPx = 21f, pageHeightPx = 280)

        assertTrue("Zooming in should increase page count", zoomedInPages.size > normalPages.size)
        assertTrue("Zooming out should decrease page count", zoomedOutPages.size < normalPages.size)
        assertTrue(
            "Zooming in should reduce words per page",
            zoomedInPages.first().wordCount < normalPages.first().wordCount
        )
        assertTrue(
            "Zooming out should increase words per page",
            zoomedOutPages.first().wordCount > normalPages.first().wordCount
        )
    }

    @Test
    fun parseEpubStream_parsesAll20BundledEpubFilesIntoPaginatedChapters() {
        val bookDataDir = File("src/main/assets/book_data").takeIf { it.isDirectory }
            ?: File("app/src/main/assets/book_data")
        val epubFiles = bookDataDir.listFiles { f -> f.extension.equals("epub", ignoreCase = true) }
            ?.sortedBy { it.name }
            .orEmpty()

        assertEquals(20, epubFiles.size)

        for (epubFile in epubFiles) {
            val chapters = epubFile.inputStream().use { stream ->
                EpubBookLoader.parseEpubStream(stream, wordsPerPage = 300)
            }
            assertTrue("Expected chapters for ${epubFile.name}", chapters.isNotEmpty())
            assertTrue(
                "Expected non-empty pages for ${epubFile.name}",
                chapters.all { ch -> ch.pages.isNotEmpty() && ch.pages.all { it.isNotBlank() } }
            )
        }
    }
}
