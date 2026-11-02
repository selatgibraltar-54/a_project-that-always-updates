package com.apksafe.cardgame.data.db

import java.security.MessageDigest
import java.security.SecureRandom

object SecurityUtils {
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val combined = "$password:$salt"
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(combined.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val calculated = hashPassword(password, salt)
        return calculated == expectedHash
    }
}
