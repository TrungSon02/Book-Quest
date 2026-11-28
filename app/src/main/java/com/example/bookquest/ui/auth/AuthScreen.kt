package com.example.bookquest.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookquest.ui.theme.BookQuestTheme
import data.AppDatabase
import data.entity.UserEntity
import kotlinx.coroutines.launch

enum class AuthMode {
    LOGIN,
    SIGN_UP
}

@Composable
fun AuthRoute(
    onAuthenticated: (user: UserEntity, destination: PostAuthDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authRepository = remember {
        val userDao = AppDatabase.get(context).userDao()
        AuthRepository(RoomUserAuthGateway(userDao))
    }

    AuthScreen(
        onLoginSubmit = { identifier, password ->
            authRepository.login(identifier, password)
        },
        onRegisterSubmit = { email, username, password ->
            authRepository.register(email, username, password)
        },
        onAuthenticated = onAuthenticated,
        modifier = modifier
    )
}

@Composable
fun AuthScreen(
    onLoginSubmit: suspend (identifier: String, password: String) -> LoginResult,
    onRegisterSubmit: suspend (email: String, username: String, password: String) -> RegisterResult,
    onAuthenticated: (user: UserEntity, destination: PostAuthDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by rememberSaveable { mutableStateOf(AuthMode.LOGIN) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Login form state
    var loginIdentifier by rememberSaveable { mutableStateOf("") }
    var loginPassword by rememberSaveable { mutableStateOf("") }
    var loginIdentifierError by rememberSaveable { mutableStateOf<String?>(null) }
    var loginPasswordError by rememberSaveable { mutableStateOf<String?>(null) }
    var showLoginPassword by rememberSaveable { mutableStateOf(false) }

    // Sign-up form state
    var signUpEmail by rememberSaveable { mutableStateOf("") }
    var signUpUsername by rememberSaveable { mutableStateOf("") }
    var signUpPassword by rememberSaveable { mutableStateOf("") }
    var signUpEmailError by rememberSaveable { mutableStateOf<String?>(null) }
    var signUpUsernameError by rememberSaveable { mutableStateOf<String?>(null) }
    var signUpPasswordError by rememberSaveable { mutableStateOf<String?>(null) }
    var showSignUpPassword by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun submitLogin() {
        if (isSubmitting) return
        scope.launch {
            isSubmitting = true
            try {
                when (val result = onLoginSubmit(loginIdentifier, loginPassword)) {
                    is LoginResult.Success -> {
                        loginIdentifierError = null
                        loginPasswordError = null
                        onAuthenticated(result.user, result.destination)
                    }
                    is LoginResult.ValidationError -> {
                        loginIdentifierError = result.identifierError
                        loginPasswordError = result.passwordError
                    }
                }
            } finally {
                isSubmitting = false
            }
        }
    }

    fun submitRegister() {
        if (isSubmitting) return
        scope.launch {
            isSubmitting = true
            try {
                when (val result = onRegisterSubmit(signUpEmail, signUpUsername, signUpPassword)) {
                    is RegisterResult.Success -> {
                        signUpEmailError = null
                        signUpUsernameError = null
                        signUpPasswordError = null
                        onAuthenticated(result.user, PostAuthDestination.PREFERENCES)
                    }
                    is RegisterResult.ValidationError -> {
                        signUpEmailError = result.emailError
                        signUpUsernameError = result.usernameError
                        signUpPasswordError = result.passwordError
                    }
                }
            } finally {
                isSubmitting = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Book Quest",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (mode == AuthMode.LOGIN) {
                "Welcome back. Log in to continue reading."
            } else {
                "Create your account to start your reading quest."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (mode == AuthMode.LOGIN) "Log in" else "Sign up",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (mode == AuthMode.LOGIN) {
                    AuthInputField(
                        value = loginIdentifier,
                        onValueChange = {
                            loginIdentifier = it
                            if (loginIdentifierError != null) loginIdentifierError = null
                        },
                        label = "Username or Email",
                        errorText = loginIdentifierError,
                        enabled = !isSubmitting,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    AuthPasswordField(
                        value = loginPassword,
                        onValueChange = {
                            loginPassword = it
                            if (loginPasswordError != null) loginPasswordError = null
                        },
                        label = "Password",
                        visible = showLoginPassword,
                        onToggleVisibility = { showLoginPassword = !showLoginPassword },
                        errorText = loginPasswordError,
                        enabled = !isSubmitting,
                        imeAction = ImeAction.Done,
                        onDone = ::submitLogin
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = ::submitLogin,
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Log in", fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    AuthInputField(
                        value = signUpEmail,
                        onValueChange = {
                            signUpEmail = it
                            if (signUpEmailError != null) signUpEmailError = null
                        },
                        label = "Email",
                        errorText = signUpEmailError,
                        enabled = !isSubmitting,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    AuthInputField(
                        value = signUpUsername,
                        onValueChange = {
                            signUpUsername = it
                            if (signUpUsernameError != null) signUpUsernameError = null
                        },
                        label = "Username",
                        errorText = signUpUsernameError,
                        enabled = !isSubmitting,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    AuthPasswordField(
                        value = signUpPassword,
                        onValueChange = {
                            signUpPassword = it
                            if (signUpPasswordError != null) signUpPasswordError = null
                        },
                        label = "Password (min 4 characters)",
                        visible = showSignUpPassword,
                        onToggleVisibility = { showSignUpPassword = !showSignUpPassword },
                        errorText = signUpPasswordError,
                        enabled = !isSubmitting,
                        imeAction = ImeAction.Done,
                        onDone = ::submitRegister
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = ::submitRegister,
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Sign up", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (mode == AuthMode.LOGIN) {
                    "Don't have an account?"
                } else {
                    "Already have an account?"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                enabled = !isSubmitting,
                onClick = {
                    if (mode == AuthMode.LOGIN) {
                        loginIdentifierError = null
                        loginPasswordError = null
                        mode = AuthMode.SIGN_UP
                    } else {
                        signUpEmailError = null
                        signUpUsernameError = null
                        signUpPasswordError = null
                        mode = AuthMode.LOGIN
                    }
                }
            ) {
                Text(
                    text = if (mode == AuthMode.LOGIN) "Sign up" else "Log in",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorText: String?,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        isError = errorText != null,
        supportingText = errorText?.let { msg ->
            {
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    errorText: String?,
    enabled: Boolean,
    imeAction: ImeAction,
    onDone: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        isError = errorText != null,
        supportingText = errorText?.let { msg ->
            {
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onClick = onToggleVisibility, enabled = enabled) {
                Text(if (visible) "Hide" else "Show")
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    BookQuestTheme {
        AuthScreen(
            onLoginSubmit = { _, _ -> LoginResult.ValidationError() },
            onRegisterSubmit = { _, _, _ -> RegisterResult.ValidationError() },
            onAuthenticated = { _, _ -> }
        )
    }
}
