package com.example.bookquest.ui.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.bookquest.ui.theme.BookQuestTheme

@Composable
fun QuestionDialog(
    questions: List<ReadingQuestion>,
    onDismiss: () -> Unit,
    showSummary: Boolean = true,
    onAnswered: (correct: Boolean) -> Unit = {},
    onDone: () -> Unit = onDismiss
) {
    var questionIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }
    var checked by rememberSaveable { mutableStateOf(false) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    var finished by rememberSaveable { mutableStateOf(false) }

    val question = questions[questionIndex]
    val title = when {
        finished -> "Result"
        questions.size == 1 -> "Question"
        else -> "Question ${questionIndex + 1} / ${questions.size}"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (finished) {
                        Text(
                            text = "Your score: $score / ${questions.size}",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                            Text("Close")
                        }
                    } else {
                        Text(
                            text = question.question,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        question.options.forEachIndexed { index, option ->
                            val isSelected = selectedIndex == index
                            val containerColor = when {
                                checked && index == question.correctIndex -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                                checked && isSelected -> Color(0xFFF44336).copy(alpha = 0.3f)
                                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(containerColor)
                                    .selectable(
                                        selected = isSelected,
                                        enabled = !checked,
                                        role = Role.RadioButton,
                                        onClick = { selectedIndex = index }
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = null)
                                Spacer(Modifier.width(12.dp))
                                Text(text = option, style = MaterialTheme.typography.bodyLarge)
                            }
                        }

                        if (checked) {
                            val correct = selectedIndex == question.correctIndex
                            Text(
                                text = if (correct) {
                                    "Correct!"
                                } else {
                                    "Incorrect. Correct answer: ${question.options[question.correctIndex]}"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (correct) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                            question.explanation?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                when {
                                    !checked -> {
                                        checked = true
                                        val correct = selectedIndex == question.correctIndex
                                        if (correct) score++
                                        onAnswered(correct)
                                    }
                                    questionIndex < questions.lastIndex -> {
                                        questionIndex++
                                        selectedIndex = -1
                                        checked = false
                                    }
                                    showSummary -> finished = true
                                    else -> onDone()
                                }
                            },
                            enabled = selectedIndex != -1,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                when {
                                    !checked -> "Check"
                                    questionIndex < questions.lastIndex -> "Next"
                                    showSummary -> "Finish"
                                    else -> "Continue"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun QuestionDialogPreview() {
    BookQuestTheme {
        QuestionDialog(
            questions = listOf(
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
                )
            ),
            onDismiss = {}
        )
    }
}