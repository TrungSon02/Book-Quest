package com.example.bookquest

import com.example.bookquest.ui.auth.AuthRepository
import com.example.bookquest.ui.auth.AuthUserGateway
import com.example.bookquest.ui.auth.LoginResult
import com.example.bookquest.ui.auth.PasswordHasher
import com.example.bookquest.ui.auth.RegisterResult
import data.entity.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private lateinit var gateway: FakeAuthUserGateway
    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        gateway = FakeAuthUserGateway()
        repository = AuthRepository(gateway)
    }

    @Test
    fun passwordHasher_hashesAndVerifiesWithoutStoringPlaintext() {
        val hash1 = PasswordHasher.hash("pass123")
        val hash2 = PasswordHasher.hash("pass123")

        assertNotEquals("pass123", hash1)
        assertNotEquals(hash1, hash2)
        assertTrue(PasswordHasher.verify("pass123", hash1))
        assertFalse(PasswordHasher.verify("wrong", hash1))
        assertFalse(PasswordHasher.verify("pass123", "not-a-valid-hash"))
    }

    @Test
    fun register_rejectsMissingFieldsInvalidEmailShortPasswordAndDuplicates() = runBlocking {
        val missing = repository.register(email = "", username = "", password = "") as RegisterResult.ValidationError
        assertNotNull(missing.emailError)
        assertNotNull(missing.usernameError)
        assertNotNull(missing.passwordError)
        assertEquals(0, gateway.users.size)

        val badEmail = repository.register("invalid-email", "alice", "1234") as RegisterResult.ValidationError
        assertNotNull(badEmail.emailError)
        assertNull(badEmail.usernameError)
        assertNull(badEmail.passwordError)

        val shortPw = repository.register("alice@example.com", "alice", "123") as RegisterResult.ValidationError
        assertNull(shortPw.emailError)
        assertNull(shortPw.usernameError)
        assertNotNull(shortPw.passwordError)

        val ok = repository.register("alice@example.com", "alice", "1234")
        assertTrue(ok is RegisterResult.Success)
        assertEquals(1, gateway.users.size)

        val dupEmail = repository.register("ALICE@example.com", "alice2", "1234") as RegisterResult.ValidationError
        assertNotNull(dupEmail.emailError)
        assertNull(dupEmail.usernameError)

        val dupUsername = repository.register("other@example.com", "Alice", "1234") as RegisterResult.ValidationError
        assertNull(dupUsername.emailError)
        assertNotNull(dupUsername.usernameError)
        assertEquals(1, gateway.users.size)
    }

    @Test
    fun login_supportsUsernameOrEmail() = runBlocking {
        val reg = repository.register("reader@bookquest.app", "bookworm", "secret") as RegisterResult.Success
        assertNotEquals("secret", reg.user.passwordHash)

        // Unknown identifier
        val unknown = repository.login("nobody", "secret") as LoginResult.ValidationError
        assertNotNull(unknown.identifierError)
        assertNull(unknown.passwordError)

        // Wrong password
        val wrongPw = repository.login("bookworm", "badpw") as LoginResult.ValidationError
        assertNull(wrongPw.identifierError)
        assertNotNull(wrongPw.passwordError)

        // Login with username
        val byUsername = repository.login("bookworm", "secret") as LoginResult.Success
        assertEquals("bookworm", byUsername.user.username)

        // Login with email (case-insensitive)
        val byEmail = repository.login("READER@bookquest.app", "secret") as LoginResult.Success
        assertEquals("reader@bookquest.app", byEmail.user.email)
    }

    private class FakeAuthUserGateway : AuthUserGateway {
        val users = mutableListOf<UserEntity>()
        private var nextId = 1L

        override suspend fun findByUsernameOrEmail(identifier: String): UserEntity? =
            users.firstOrNull {
                it.username.equals(identifier, ignoreCase = true) ||
                    it.email.equals(identifier, ignoreCase = true)
            }

        override suspend fun isEmailTaken(email: String): Boolean =
            users.any { it.email.equals(email, ignoreCase = true) }

        override suspend fun isUsernameTaken(username: String): Boolean =
            users.any { it.username.equals(username, ignoreCase = true) }

        override suspend fun insertUser(user: UserEntity): Long {
            val id = nextId++
            users += user.copy(id = id)
            return id
        }
    }
}
