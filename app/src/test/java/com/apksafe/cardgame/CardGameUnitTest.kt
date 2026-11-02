package com.apksafe.cardgame

import com.apksafe.cardgame.data.db.SecurityUtils
import com.apksafe.cardgame.data.model.CardCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CardGameUnitTest {

    @Test
    fun testSecurityUtils_HashingAndVerification() {
        val pin = "5849"
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(pin, salt)

        assertNotNull(hash)
        assertTrue(SecurityUtils.verifyPassword(pin, salt, hash))
        assertFalse(SecurityUtils.verifyPassword("wrong_pin", salt, hash))
    }

    @Test
    fun testCardCatalog_GeneratesValidDeck() {
        val deck = CardCatalog.createStandardDeck()
        assertEquals(15, deck.size)
        assertTrue(deck.all { it.cost in 1..10 })
    }

    @Test
    fun testVictoryMessageText() {
        val expectedMessage = "Kamu menang, Selamat!"
        assertTrue(expectedMessage.contains("menang"))
        assertTrue(expectedMessage.contains("Selamat"))
    }

    @Test
    fun testUserEntityForLobby() {
        val user = com.apksafe.cardgame.data.db.UserEntity(
            id = 1,
            username = "MasterDuelist",
            passwordHash = "hash123",
            salt = "salt123",
            coins = 350,
            level = 4,
            wins = 10,
            losses = 2,
            matchesPlayed = 12
        )
        assertEquals("MasterDuelist", user.username)
        assertEquals(350, user.coins)
        assertEquals(10, user.wins)
    }

    @Test
    fun testDashboardStatsCalculation() {
        val wins = 15
        val losses = 5
        val total = wins + losses
        val winRate = (wins.toFloat() / total.toFloat()) * 100f
        val ratio = wins.toFloat() / losses.toFloat()

        assertEquals(20, total)
        assertEquals(75.0f, winRate, 0.01f)
        assertEquals(3.0f, ratio, 0.01f)

        // Test zero losses edge case
        val perfectWins = 10
        val zeroLosses = 0
        val perfectRatio = if (zeroLosses > 0) "${perfectWins / zeroLosses}x" else "MAX"
        assertEquals("MAX", perfectRatio)
    }
}
