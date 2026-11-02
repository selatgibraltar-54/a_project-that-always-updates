package com.apksafe.cardgame.data.model

enum class CardElement(val label: String, val colorHex: Long) {
    FIRE("Fire", 0xFFE64A19),
    WATER("Water", 0xFF0288D1),
    NATURE("Nature", 0xFF388E3C),
    LIGHTNING("Lightning", 0xFFFBC02D),
    VOID("Void", 0xFF7B1FA2)
}

enum class CardRarity(val label: String, val colorHex: Long) {
    COMMON("Common", 0xFF9E9E9E),
    RARE("Rare", 0xFF2196F3),
    EPIC("Epic", 0xFF9C27B0),
    LEGENDARY("Legendary", 0xFFFFD700)
}

enum class CardType(val label: String) {
    CREATURE("Creature"),
    SPELL("Spell"),
    SHIELD("Shield")
}

data class Card(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val attack: Int,
    val defense: Int,
    val element: CardElement,
    val rarity: CardRarity,
    val type: CardType,
    val currentHp: Int = defense,
    val canAttack: Boolean = false,
    val hasTaunt: Boolean = false
)

data class Player(
    val id: String,
    val name: String,
    val avatar: String,
    val isBot: Boolean = false,
    val health: Int = 30,
    val maxHealth: Int = 30,
    val shield: Int = 0,
    val energy: Int = 2,
    val maxEnergy: Int = 2,
    val hand: List<Card> = emptyList(),
    val field: List<Card> = emptyList(),
    val deck: List<Card> = emptyList(),
    val graveyard: List<Card> = emptyList(),
    val afkSwitches: Int = 0
)

enum class GamePhase {
    PLAYING,
    PASSING_TURN,
    GAME_OVER
}

data class GameState(
    val player1: Player,
    val player2: Player,
    val activePlayerIndex: Int = 0, // 0 for player1, 1 for player2
    val roundNumber: Int = 1,
    val phase: GamePhase = GamePhase.PLAYING,
    val afkTimeRemainingSeconds: Int = 240, // Exactly 4 minutes = 240 seconds
    val afkTotalDurationSeconds: Int = 240, // 240 for regular 4-min rule, 10 for fast test mode
    val isFastTestMode: Boolean = false,
    val afkNotice: String? = null,
    val winnerName: String? = null,
    val isWinnerBot: Boolean = false,
    val gameOverReason: String? = null,
    val logs: List<String> = emptyList()
) {
    val activePlayer: Player
        get() = if (activePlayerIndex == 0) player1 else player2

    val waitingPlayer: Player
        get() = if (activePlayerIndex == 0) player2 else player1
}
