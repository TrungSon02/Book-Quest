package com.example.bookquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bookquest.ui.HomePage
import com.example.bookquest.ui.auth.AuthRoute
import com.example.bookquest.ui.reading.ReadingBookRoute
import com.example.bookquest.ui.theme.BookQuestTheme
import data.AppDatabase

private const val PREFERENCE_SCREEN_IMPLEMENTED = false

@Composable
fun BookQuestApp() {
    var authenticatedUserId by rememberSaveable { mutableStateOf<Long?>(null) }
    var needsPreferences by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedBookId by rememberSaveable { mutableStateOf<Long?>(null) }

    val currentUserId = authenticatedUserId
    if (currentUserId == null) {
        AuthRoute(
            onAuthenticated = { user, isNewUser ->
                authenticatedUserId = user.id
                // UC-001: new registrations (and unconfigured users) go to Preference Page once implemented;
                // until then, fall back directly to HomePage.
                needsPreferences = PREFERENCE_SCREEN_IMPLEMENTED && isNewUser
                selectedTab = 0
                selectedBookId = null
            }
        )
        return
    }

    if (PREFERENCE_SCREEN_IMPLEMENTED && needsPreferences) {
        // TODO: Render PreferenceScreen(userId = currentUserId, onCompleted = { needsPreferences = false })
        needsPreferences = false
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
                    icon = { Icon(painter = painterResource(R.drawable.account), contentDescription = null) },
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomePage(
                modifier = Modifier.padding(innerPadding),
                onBookClick = { bookId -> selectedBookId = bookId }
            )
            1 -> AccountTab(
                userId = currentUserId,
                onLogout = {
                    authenticatedUserId = null
                    needsPreferences = false
                    selectedBookId = null
                    selectedTab = 0
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun AccountTab(
    userId: Long,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userDao = remember { AppDatabase.get(context).userDao() }
    val user by remember(userId) { userDao.observeUserById(userId) }.collectAsState(initial = null)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Account",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = user?.username ?: "Loading...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user?.email.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("English Level: ${user?.englishLevel ?: "-"}")
                Text("Coins: ${user?.coinBalance ?: 0}")
                Text("Reading Streak: ${user?.currentReadingStreak ?: 0} days")
                Text("21-Day Habit Mode: ${if (user?.habit21dEnabled == true) "Enabled" else "Off"}")
            }
        }
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log out")
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
