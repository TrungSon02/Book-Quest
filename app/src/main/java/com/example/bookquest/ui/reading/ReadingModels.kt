package com.example.bookquest.ui.reading

data class ReadingQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String? = null
)

data class ReadingChapter(
    val title: String,
    val pages: List<String>,
    val questions: List<ReadingQuestion> = emptyList(),
    val pageQuestions: List<ReadingQuestion?> = emptyList()
)

data class ReadingBook(
    val title: String,
    val author: String,
    val coverImage: String? = null,
    val chapters: List<ReadingChapter>
)