package com.example.bookquest.ui.auth

import data.dao.UserDao
import data.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.TimeZone

enum class PostAuthDestination {
    PREFERENCES,
    HOME
}

sealed interface LoginResult {
    data class Success(
        val user: UserEntity,
        val destination: PostAuthDestination
    ) : LoginResult

    data class ValidationError(
        val identifierError: String? = null,
        val passwordError: String? = null
    ) : LoginResult
}

sealed interface RegisterResult {
    data class Success(val user: UserEntity) : RegisterResult

    data class ValidationError(
        val emailError: String? = null,
        val usernameError: String? = null,
        val passwordError: String? = null
    ) : RegisterResult
}

interface AuthUserGateway {
    suspend fun findByUsernameOrEmail(identifier: String): UserEntity?
    suspend fun isEmailTaken(email: String): Boolean
    suspend fun isUsernameTaken(username: String): Boolean
    suspend fun insertUser(user: UserEntity): Long
}

class RoomUserAuthGateway(
    private val userDao: UserDao
) : AuthUserGateway {
    override suspend fun findByUsernameOrEmail(identifier: String): UserEntity? =
        userDao.getUserByUsernameOrEmail(identifier)

    override suspend fun isEmailTaken(email: String): Boolean =
        userDao.isEmailTaken(email)

    override suspend fun isUsernameTaken(username: String): Boolean =
        userDao.isUsernameTaken(username)

    override suspend fun insertUser(user: UserEntity): Long =
        userDao.insertUser(user)
}

/**
 * Handles UC-001 validation, password hashing, registration, and login routing.
 */
class AuthRepository(
    private val gateway: AuthUserGateway
) {
    suspend fun login(
        identifier: String,
        password: String
    ): LoginResult = withContext(Dispatchers.IO) {
        val cleanIdentifier = identifier.trim()

        val identifierRequiredError = if (cleanIdentifier.isEmpty()) {
            "Please enter your username or email"
        } else {
            null
        }
        val passwordRequiredError = if (password.isEmpty()) {
            "Please enter your password"
        } else {
            null
        }

        if (identifierRequiredError != null || passwordRequiredError != null) {
            return@withContext LoginResult.ValidationError(
                identifierError = identifierRequiredError,
                passwordError = passwordRequiredError
            )
        }

        val user = gateway.findByUsernameOrEmail(cleanIdentifier)
            ?: return@withContext LoginResult.ValidationError(
                identifierError = "No account found with this username or email"
            )

        if (!PasswordHasher.verify(password, user.passwordHash)) {
            return@withContext LoginResult.ValidationError(
                passwordError = "Incorrect password"
            )
        }

        val destination = if (user.preferencesCompleted) {
            PostAuthDestination.HOME
        } else {
            PostAuthDestination.PREFERENCES
        }
        LoginResult.Success(user = user, destination = destination)
    }

    suspend fun register(
        email: String,
        username: String,
        password: String
    ): RegisterResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        val cleanUsername = username.trim()

        var emailError: String? = when {
            cleanEmail.isEmpty() -> "Email is required"
            !EMAIL_REGEX.matches(cleanEmail) -> "Please enter a valid email address"
            else -> null
        }

        var usernameError: String? = when {
            cleanUsername.isEmpty() -> "Username is required"
            else -> null
        }

        val passwordError: String? = when {
            password.isEmpty() -> "Password is required"
            password.length < MIN_PASSWORD_LENGTH ->
                "Password must be at least $MIN_PASSWORD_LENGTH characters"
            else -> null
        }

        if (emailError == null && gateway.isEmailTaken(cleanEmail)) {
            emailError = "This email is already registered"
        }
        if (usernameError == null && gateway.isUsernameTaken(cleanUsername)) {
            usernameError = "This username is already taken"
        }

        if (emailError != null || usernameError != null || passwordError != null) {
            return@withContext RegisterResult.ValidationError(
                emailError = emailError,
                usernameError = usernameError,
                passwordError = passwordError
            )
        }

        val newUser = UserEntity(
            username = cleanUsername,
            email = cleanEmail,
            passwordHash = PasswordHasher.hash(password),
            englishLevel = "B1",
            timezone = TimeZone.getDefault().id,
            dailyReadingGoal = 15,
            accountRole = "learner",
            coinBalance = 0,
            currentReadingStreak = 0,
            lastDayStreakClaimed = null,
            habit21dEnabled = false,
            preferencesCompleted = false
        )

        val createdId = gateway.insertUser(newUser)
        RegisterResult.Success(user = newUser.copy(id = createdId))
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 4
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
