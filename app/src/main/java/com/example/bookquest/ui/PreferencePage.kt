package com.example.bookquest.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.AppDatabase
import data.entity.EnglishLevel
import data.entity.ReadingGoal
import kotlinx.coroutines.launch

@Composable
fun PreferencePage(
    userId: Long,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
){
    var goal by rememberSaveable { mutableStateOf<ReadingGoal?>(null) }
    var level by rememberSaveable { mutableStateOf<EnglishLevel?>(null) }
    var selectedGenreIds by rememberSaveable { mutableStateOf<Set<Long>>(emptySet()) }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    //Get a Context so we can open the database
    val context = LocalContext.current

    //Open the database once, and get the DAO from it
    val database = remember {
        AppDatabase.get(context)
    }
    val preferenceDao = remember {
        database.preferenceDao()
    }
    val scope = rememberCoroutineScope()

    // Genres for the chips, read from the database
    val genresFlow = remember {
        preferenceDao.getGenres()
    }
    val genres by genresFlow.collectAsState(initial = emptyList())

    // Continue is only enabled when everything is chosen
    val chosenGoal = goal
    val chosenLevel = level
    val canSubmit = chosenGoal != null && chosenLevel != null
            && selectedGenreIds.isNotEmpty() && !isSaving

    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier.fillMaxSize().systemBarsPadding().padding(16.dp)
    ) {
        //Title
        Text("Personalize your reading", fontWeight = FontWeight.Bold, fontSize = 30.sp)

        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
        ) {
            //Reading goal
            PreferenceSection("What is your reading goal?") {
                ReadingGoal.entries.forEach { option ->
                    GoalCard(
                        title = option.label,
                        description = option.description,
                        selected = goal == option,
                        onClick = { goal = option }
                    )
                }
            }

            //English level
            PreferenceSection("What is your English level?") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnglishLevel.entries.forEach { option ->
                        FilterChip(
                            selected = level == option,
                            onClick = { level = option },
                            label = { Text(option.label) }
                        )
                    }
                }
            }

            //Genres
            PreferenceSection("Pick your favorite genres", hint = "Choose at least one") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genres.forEach { genre ->
                        val genreId = genre.id ?: return@forEach
                        FilterChip(
                            selected = genreId in selectedGenreIds,
                            onClick = {
                                selectedGenreIds = if (genreId in selectedGenreIds) {
                                    selectedGenreIds - genreId
                                } else {
                                    selectedGenreIds + genreId
                                }
                            },
                            label = { Text(genre.name) }
                        )
                    }
                }
            }
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        //Continue button
        Button(
            onClick = {
                if (chosenGoal != null && chosenLevel != null) {
                    scope.launch {
                        isSaving = true
                        error = null
                        try {
                            preferenceDao.savePreferences(
                                userId = userId,
                                goal = chosenGoal.name,
                                level = chosenLevel.name,
                                genreIds = selectedGenreIds.toList()
                            )
                            onFinished()
                        } catch (e: Exception) {
                            error = e.message ?: "Could not save"
                        }
                        isSaving = false
                    }
                }
            },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (isSaving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Continue")
        }
    }
}

@Composable
private fun PreferenceSection(
    title: String,
    hint: String? = null,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        hint?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        content()
    }
}

@Composable
private fun GoalCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(selected = selected, onClick = null)
        }
    }
}