package com.example.bookquest.ui.auth

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hashes and verifies passwords using salted PBKDF2 so plaintext passwords are never persisted.
 */
object PasswordHasher {

    private const val SALT_BYTES = 16
    private const val KEY_LENGTH_BITS = 256
    private const val ITERATIONS = 65_536
    private const val ALG_SHA256 = "pbkdf2_sha256"
    private const val ALG_SHA1 = "pbkdf2_sha1"

    private val secureRandom = SecureRandom()

    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES)
        secureRandom.nextBytes(salt)
        val (algLabel, digest) = deriveKey(password, salt, ITERATIONS, preferSha256 = true)
        return "$algLabel$$ITERATIONS$${salt.toHex()}$${digest.toHex()}"
    }

    fun verify(password: String, storedHash: String): Boolean {
        val parts = storedHash.split('$')
        if (parts.size != 4) return false

        val algLabel = parts[0]
        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = parts[2].fromHex() ?: return false
        val expectedDigest = parts[3].fromHex() ?: return false

        val useSha256 = when (algLabel) {
            ALG_SHA256 -> true
            ALG_SHA1 -> false
            else -> return false
        }

        val (_, actualDigest) = deriveKey(
            password = password,
            salt = salt,
            iterations = iterations,
            preferSha256 = useSha256,
            keyLengthBits = expectedDigest.size * 8
        )
        return MessageDigest.isEqual(expectedDigest, actualDigest)
    }

    private fun deriveKey(
        password: String,
        salt: ByteArray,
        iterations: Int,
        preferSha256: Boolean,
        keyLengthBits: Int = KEY_LENGTH_BITS
    ): Pair<String, ByteArray> {
        val jcaAlg = if (preferSha256) "PBKDF2WithHmacSHA256" else "PBKDF2WithHmacSHA1"
        return try {
            val factory = SecretKeyFactory.getInstance(jcaAlg)
            val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLengthBits)
            val encoded = factory.generateSecret(spec).encoded
            (if (preferSha256) ALG_SHA256 else ALG_SHA1) to encoded
        } catch (_: NoSuchAlgorithmException) {
            val fallbackFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLengthBits)
            ALG_SHA1 to fallbackFactory.generateSecret(spec).encoded
        }
    }

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte) }

    private fun String.fromHex(): ByteArray? {
        if (length % 2 != 0) return null
        return ByteArray(length / 2) { index ->
            val hi = this[index * 2].digitToIntOrNull(16) ?: return null
            val lo = this[index * 2 + 1].digitToIntOrNull(16) ?: return null
            ((hi shl 4) or lo).toByte()
        }
    }
}
