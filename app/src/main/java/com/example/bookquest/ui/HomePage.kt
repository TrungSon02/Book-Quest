package com.example.bookquest.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookquest.R
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import data.AppDatabase

private val motivationQuotes = listOf(
    "A book is a gift you can open again and again",
    "We read to know we are not alone",
    "The greatest gift is a passion for reading",
    "Books are a uniquely portable magic",
    "Books are lighthouses erected in the great sea of time",
    "Reading is to the mind what exercise is to the body",
    "A house without books is like a room without windows",
    "He that loves reading has everything within his reach",
)

//Testing data
private val englishLevels = listOf("A1", "A2", "B1", "B2", "C1", "C2")

@Composable
fun HomePage(modifier: Modifier = Modifier){
    var query by rememberSaveable { mutableStateOf("") }
    val quote = rememberSaveable { motivationQuotes.random() }
    var recommendedOnly by rememberSaveable { mutableStateOf(false) }
    var genre by rememberSaveable { mutableStateOf<String?>(null) }
    var level by rememberSaveable { mutableStateOf<String?>(null) }

    //Get a Context so we can open the database
    val context = LocalContext.current

    //Open the database once, and get the DAO from it
    val database = remember {
        AppDatabase.get(context)
    }
    val bookDao = remember {
        database.bookDao()
    }

    // Ask the database again whenever query, genre or level changes
    val booksFlow = remember(query, genre, level) {
        bookDao.searchBooks(
            searchText = query.trim(),
            level = level,
            genreName = genre
        )
    }
    val books by booksFlow.collectAsState(initial = emptyList())

    // Genre names for the dropdown, read from the database
    val genreNamesFlow = remember {
        bookDao.getGenreNames()
    }
    val genreNames by genreNamesFlow.collectAsState(initial = emptyList())

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        //Title
        Text("Book Quest", fontWeight = FontWeight.Bold, fontSize = 30.sp)
        Text(
            text = quote,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )


        //Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search books, authors...") },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.search_icon),
                    contentDescription = null

                )
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        )

        //Filter
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = recommendedOnly,
                onClick = { recommendedOnly = !recommendedOnly },
                label = { Text("Recommended") }
            )
            FilterDropdownChip("Genre", genreNames, genre) { genre = it }
            FilterDropdownChip("Level", englishLevels, level) { level = it }
        }

        if(1==2) { //Change with the checking if user has read any books afterward
            //Continue where u left off
            Text(
                "Continue Your Quest!",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .align(Alignment.Start)
            )

            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        //Book list
        LazyVerticalStaggeredGrid(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalItemSpacing = 24.dp,
            columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(
                items = books,
                key = { bookWithGenres -> bookWithGenres.book.id ?: 0L }
            ) { bookWithGenres ->

                // Take the pieces out of the combined object
                val book = bookWithGenres.book
                val genreNames = bookWithGenres.genres.map { genre -> genre.name }

                // Pass them to the card one by one
                BookBox(
                    coverPath = book.coverPath,
                    title = book.title,
                    author = book.author,
                    engLevel = book.level,
                    description = book.description,
                    genres = genreNames,
                    progress = 0
                )
            }
        }
    }
}

@Composable
private fun FilterDropdownChip(
    label: String,
    options: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = selected != null,
            onClick = { expanded = true },
            label = { Text("${selected ?: label} ▾") }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = { onSelect(null); expanded = false }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

@Composable
fun BookBox(
    modifier: Modifier = Modifier,
    coverPath: String = "",
    engLevel: String = "C1",
    progress: Int = 0,
    title: String = "",
    author: String = "",
    genres: List<String> = listOf("Self-help"),
    description: String = "",
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            ){
                //Image Cover
                AsyncImage(
                    model = "file:///android_asset/$coverPath",
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.sample_cover),
                    error = painterResource(R.drawable.sample_cover),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(12.dp))
                )
                if (progress != 0) {
                    ImageBadge("$progress%", Modifier.align(Alignment.BottomStart))
                }
                ImageBadge(engLevel, Modifier.align(Alignment.BottomEnd))
            }

            //Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            //Author
            Text(
                text = "By $author",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            //Genres
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                genres.forEach { genreName ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = genreName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 2.dp
                            )
                        )
                    }
                }
            }

            //Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ImageBadge(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(6.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.65f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}