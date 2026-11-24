package com.example.bookquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.bookquest.ui.HomePage
import com.example.bookquest.ui.PreferencePage
import com.example.bookquest.ui.reading.ReadingBookRoute
import com.example.bookquest.ui.theme.BookQuestTheme

@Composable
fun BookQuestApp(){
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedBookId by rememberSaveable { mutableStateOf<Long?>(null) }
    //Set this to a new user's id to open the preference page
    var preferenceUserId by rememberSaveable { mutableStateOf<Long?>(null) }

    val activePreferenceUserId = preferenceUserId
    if (activePreferenceUserId != null) {
        PreferencePage(
            userId = activePreferenceUserId,
            onFinished = { preferenceUserId = null }
        )
        return
    }

    val activeBookId = selectedBookId
    if (activeBookId != null) {
        ReadingBookRoute(
            bookId = activeBookId,
            onBackClick = { selectedBookId = null }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(painter = painterResource(R.drawable.home), contentDescription = null) },
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(painterResource(R.drawable.account), contentDescription = null) },
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomePage(
                modifier = Modifier.padding(innerPadding),
                onBookClick = { bookId -> selectedBookId = bookId }
            )
            1 -> Text("Account", Modifier.padding(innerPadding)) // placeholder

        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookQuestTheme {
                BookQuestApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BookQuestTheme {
        HomePage()
    }
}

//@Preview(showBackground = true)
//@Composable
//fun BookBoxPreview() {
//    BookQuestTheme {
//        BookBox()
//    }
//}