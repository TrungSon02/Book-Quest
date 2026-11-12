package com.example.bookquest.ui.reading

data class ReadingChapter(
    val title: String,
    val pages: List<String>
)

data class ReadingBook(
    val title: String,
    val author: String,
    val coverImage: String? = null,
    val chapters: List<ReadingChapter>
)