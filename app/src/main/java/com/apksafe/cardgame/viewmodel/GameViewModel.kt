package com.apksafe.cardgame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apksafe.cardgame.data.db.UserEntity
import com.apksafe.cardgame.data.model.Card
import com.apksafe.cardgame.data.model.CardCatalog
import com.apksafe.cardgame.data.model.CardType
import com.apksafe.cardgame.data.model.GamePhase
import com.apksafe.cardgame.data.model.GameState
import com.apksafe.cardgame.data.model.Player
import com.apksafe.cardgame.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(private val repository: UserRepository) : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _selectedHandCard = MutableStateFlow<Card?>(null)
    val selectedHandCard: StateFlow<Card?> = _selectedHandCard.asStateFlow()

    private val _selectedAttacker = MutableStateFlow<Card?>(null)
    val selectedAttacker: StateFlow<Card?> = _selectedAttacker.asStateFlow()

    private var timerJob: Job? = null
    private var botJob: Job? = null

    companion object {
        const val STANDARD_AFK_SECONDS = 240 // 4 Minutes
        const val FAST_TEST_AFK_SECONDS = 10 // For rapid demonstration
    }

    fun startMatch(player1User: UserEntity, player2User: UserEntity?, isBot: Boolean) {
        val p1Deck = CardCatalog.createStandardDeck()
        val p2Deck = CardCatalog.createStandardDeck()

        val p1Hand = p1Deck.take(4)
        val p1RemainingDeck = p1Deck.drop(4)

        val p2Hand = p2Deck.take(4)
        val p2RemainingDeck = p2Deck.drop(4)

        val p1 = Player(
            id = "p1_${player1User.id}",
            name = player1User.username,
            avatar = player1User.avatar,
            isBot = false,
            health = 30,
            maxHealth = 30,
            shield = 0,
            energy = 3,
            maxEnergy = 3,
            hand = p1Hand,
            field = emptyList(),
            deck = p1RemainingDeck
        )

        val p2 = Player(
            id = if (isBot) "bot" else "p2_${player2User?.id ?: 2}",
            name = if (isBot) "AI Bot (Training)" else (player2User?.username ?: "PlayerTwo"),
            avatar = if (isBot) "shadow" else (player2User?.avatar ?: "phoenix"),
            isBot = isBot,
            health = 30,
            maxHealth = 30,
            shield = 0,
            energy = 3,
            maxEnergy = 3,
            hand = p2Hand,
            field = emptyList(),
            deck = p2RemainingDeck
        )

        val initial = GameState(
            player1 = p1,
            player2 = p2,
            activePlayerIndex = 0,
            roundNumber = 1,
            phase = GamePhase.PLAYING,
            afkTimeRemainingSeconds = STANDARD_AFK_SECONDS,
            afkTotalDurationSeconds = STANDARD_AFK_SECONDS,
            isFastTestMode = false,
            afkNotice = null,
            logs = listOf("Pertandingan dimulai! Giliran ${p1.name}.")
        )

        _gameState.value = initial
        _selectedHandCard.value = null
        _selectedAttacker.value = null

        startAfkTimer()
    }

    fun toggleFastAfkTestMode() {
        _gameState.update { current ->
            if (current == null) return@update null
            val newFast = !current.isFastTestMode
            val newDuration = if (newFast) FAST_TEST_AFK_SECONDS else STANDARD_AFK_SECONDS
            current.copy(
                isFastTestMode = newFast,
                afkTotalDurationSeconds = newDuration,
                afkTimeRemainingSeconds = newDuration,
                logs = current.logs + listOf(
                    if (newFast) "⚡ Mode Tes AFK Aktif (10 Detik untuk pengujian cepat)"
                    else "⏱️ Mode Standar Aktif (Aturan penuh 4 Menit)"
                )
            )
        }
    }

    private fun startAfkTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val state = _gameState.value ?: break
                if (state.phase != GamePhase.PLAYING) continue

                if (state.afkTimeRemainingSeconds > 1) {
                    _gameState.update { it?.copy(afkTimeRemainingSeconds = it.afkTimeRemainingSeconds - 1) }
                } else {
                    // Trigger AFK switch! (exceeded 4 minutes)
                    handleAfkTimeout()
                }
            }
        }
    }

    private fun handleAfkTimeout() {
        val state = _gameState.value ?: return
        val currentActive = state.activePlayer
        val nextActive = state.waitingPlayer

        val notice = "⚠️ AFK SWITCH (> 4 Menit)! ${currentActive.name} tidak aktif. Giliran otomatis dialihkan ke ${nextActive.name}."

        // Update player AFK switches count and pass turn
        val updatedP1 = if (state.activePlayerIndex == 0) state.player1.copy(afkSwitches = state.player1.afkSwitches + 1) else state.player1
        val updatedP2 = if (state.activePlayerIndex == 1) state.player2.copy(afkSwitches = state.player2.afkSwitches + 1) else state.player2

        _gameState.update {
            it?.copy(
                player1 = updatedP1,
                player2 = updatedP2,
                afkNotice = notice,
                logs = it.logs + listOf(notice)
            )
        }

        switchTurnInternal(dueToAfk = true)
    }

    fun onUserActivity() {
        // Any user interaction resets the AFK timer
        _gameState.update { state ->
            if (state == null) null
            else state.copy(afkTimeRemainingSeconds = state.afkTotalDurationSeconds)
        }
    }

    fun selectHandCard(card: Card?) {
        onUserActivity()
        _selectedHandCard.value = card
        _selectedAttacker.value = null
    }

    fun selectAttacker(card: Card?) {
        onUserActivity()
        _selectedAttacker.value = card
        _selectedHandCard.value = null
    }

    fun clearNotice() {
        _gameState.update { it?.copy(afkNotice = null) }
    }

    fun playSelectedCard() {
        onUserActivity()
        val card = _selectedHandCard.value ?: return
        val state = _gameState.value ?: return
        val active = state.activePlayer

        if (active.energy < card.cost) {
            _gameState.update { it?.copy(afkNotice = "Energi tidak cukup! Butuh ${card.cost} energi.") }
            return
        }

        val newHand = active.hand.filter { it.id != card.id }
        val newEnergy = active.energy - card.cost
        var newField = active.field
        var newShield = active.shield
        var newHealth = active.health
        var opponent = state.waitingPlayer
        val logs = ArrayList(state.logs)

        when (card.type) {
            CardType.CREATURE -> {
                if (newField.size >= 5) {
                    _gameState.update { it?.copy(afkNotice = "Arena penuh (maks 5 kartu)!") }
                    return
                }
                newField = newField + card.copy(canAttack = card.canAttack)
                logs.add("${active.name} memainkan makhluk '${card.name}' (ATK ${card.attack} / HP ${card.defense}).")
            }
            CardType.SPELL -> {
                if (card.attack > 0) {
                    // Deal direct damage to opponent
                    val (oppHealth, oppShield) = applyDamage(opponent.health, opponent.shield, card.attack)
                    opponent = opponent.copy(health = oppHealth, shield = oppShield)
                    logs.add("${active.name} melempar '${card.name}' menghasilkan ${card.attack} damage ke ${opponent.name}!")
                }
                if (card.defense > 0) {
                    // Heal active hero
                    val healed = (newHealth + card.defense).coerceAtMost(active.maxHealth)
                    logs.add("${active.name} menggunakan '${card.name}' memulihkan ${card.defense} HP!")
                    newHealth = healed
                }
            }
            CardType.SHIELD -> {
                newShield += card.defense
                logs.add("${active.name} memasang perisai '${card.name}' (+${card.defense} Shield).")
            }
        }

        val updatedActive = active.copy(
            energy = newEnergy,
            hand = newHand,
            field = newField,
            shield = newShield,
            health = newHealth
        )

        val updatedP1 = if (state.activePlayerIndex == 0) updatedActive else opponent
        val updatedP2 = if (state.activePlayerIndex == 1) updatedActive else opponent

        _selectedHandCard.value = null
        _gameState.update {
            it?.copy(
                player1 = updatedP1,
                player2 = updatedP2,
                logs = logs
            )
        }

        checkGameOver()
    }

    fun attackWithSelected(targetCard: Card?) {
        onUserActivity()
        val attacker = _selectedAttacker.value ?: return
        val state = _gameState.value ?: return
        val active = state.activePlayer
        val opponent = state.waitingPlayer

        if (!attacker.canAttack) {
            _gameState.update { it?.copy(afkNotice = "Kartu ini baru dimainkan atau sudah menyerang putaran ini!") }
            return
        }

        // Check taunt
        val opponentTaunts = opponent.field.filter { it.hasTaunt }
        if (opponentTaunts.isNotEmpty()) {
            if (targetCard == null || !targetCard.hasTaunt) {
                _gameState.update { it?.copy(afkNotice = "Harus menyerang kartu musuh dengan status TAUNT terlebih dahulu!") }
                return
            }
        }

        val logs = ArrayList(state.logs)
        var updatedOpponent = opponent
        var updatedAttackerField = active.field

        if (targetCard == null) {
            // Direct attack to opponent player
            val (newOppHealth, newOppShield) = applyDamage(opponent.health, opponent.shield, attacker.attack)
            updatedOpponent = opponent.copy(health = newOppHealth, shield = newOppShield)
            logs.add("${active.name}: '${attacker.name}' menyerang langsung ${opponent.name} sebesar ${attacker.attack} damage!")

            // Mark attacker as exhausted
            updatedAttackerField = updatedAttackerField.map {
                if (it.id == attacker.id) it.copy(canAttack = false) else it
            }
        } else {
            // Card vs Card combat
            logs.add("${active.name}: '${attacker.name}' (ATK ${attacker.attack}) bertarung dengan '${targetCard.name}' (ATK ${targetCard.attack})!")

            val remainingTargetHp = targetCard.currentHp - attacker.attack
            val remainingAttackerHp = attacker.currentHp - targetCard.attack

            // Update opponent field
            val newOppField = if (remainingTargetHp <= 0) {
                logs.add("Kartu musuh '${targetCard.name}' hancur!")
                opponent.field.filter { it.id != targetCard.id }
            } else {
                opponent.field.map { if (it.id == targetCard.id) it.copy(currentHp = remainingTargetHp) else it }
            }
            updatedOpponent = opponent.copy(field = newOppField)

            // Update active field
            updatedAttackerField = if (remainingAttackerHp <= 0) {
                logs.add("Kartu penyerang '${attacker.name}' tumbang!")
                updatedAttackerField.filter { it.id != attacker.id }
            } else {
                updatedAttackerField.map {
                    if (it.id == attacker.id) it.copy(currentHp = remainingAttackerHp, canAttack = false) else it
                }
            }
        }

        val updatedActive = active.copy(field = updatedAttackerField)
        val updatedP1 = if (state.activePlayerIndex == 0) updatedActive else updatedOpponent
        val updatedP2 = if (state.activePlayerIndex == 1) updatedActive else updatedOpponent

        _selectedAttacker.value = null
        _gameState.update {
            it?.copy(
                player1 = updatedP1,
                player2 = updatedP2,
                logs = logs
            )
        }

        checkGameOver()
    }

    private fun applyDamage(currentHealth: Int, currentShield: Int, damage: Int): Pair<Int, Int> {
        return if (currentShield >= damage) {
            Pair(currentHealth, currentShield - damage)
        } else {
            val leftoverDamage = damage - currentShield
            Pair((currentHealth - leftoverDamage).coerceAtLeast(0), 0)
        }
    }

    fun endTurn() {
        onUserActivity()
        switchTurnInternal(dueToAfk = false)
    }

    private fun switchTurnInternal(dueToAfk: Boolean) {
        val state = _gameState.value ?: return
        if (state.phase == GamePhase.GAME_OVER) return

        val nextIndex = if (state.activePlayerIndex == 0) 1 else 0
        val isNewRound = nextIndex == 0
        val nextRoundNum = if (isNewRound) state.roundNumber + 1 else state.roundNumber

        // Prepare the upcoming active player:
        // Ready up all their field cards so they can attack, increase max energy (up to 10), draw 1 card
        val currentWaiting = if (nextIndex == 0) state.player1 else state.player2
        val newMaxEnergy = if (isNewRound) (currentWaiting.maxEnergy + 1).coerceAtMost(10) else currentWaiting.maxEnergy
        val newEnergy = newMaxEnergy

        // Draw card if deck not empty
        var newHand = currentWaiting.hand
        var newDeck = currentWaiting.deck
        if (newDeck.isNotEmpty() && newHand.size < 7) {
            val drawn = newDeck.first()
            newDeck = newDeck.drop(1)
            newHand = newHand + drawn
        }

        // Refresh field cards
        val refreshedField = currentWaiting.field.map { it.copy(canAttack = true) }

        val readyPlayer = currentWaiting.copy(
            energy = newEnergy,
            maxEnergy = newMaxEnergy,
            hand = newHand,
            deck = newDeck,
            field = refreshedField
        )

        val updatedP1 = if (nextIndex == 0) readyPlayer else state.player1
        val updatedP2 = if (nextIndex == 1) readyPlayer else state.player2

        val logMsg = if (dueToAfk) {
            "Giliran berganti ke ${readyPlayer.name} karena timeout AFK!"
        } else {
            "Giliran ${readyPlayer.name} dimulai (Round $nextRoundNum)."
        }

        _selectedHandCard.value = null
        _selectedAttacker.value = null

        _gameState.update {
            it?.copy(
                player1 = updatedP1,
                player2 = updatedP2,
                activePlayerIndex = nextIndex,
                roundNumber = nextRoundNum,
                afkTimeRemainingSeconds = it.afkTotalDurationSeconds,
                phase = if (readyPlayer.isBot) GamePhase.PLAYING else GamePhase.PASSING_TURN,
                logs = it.logs + listOf(logMsg)
            )
        }

        // If next is bot, trigger bot decision
        if (readyPlayer.isBot) {
            triggerBotTurn()
        }
    }

    fun dismissPassDeviceScreen() {
        onUserActivity()
        _gameState.update { it?.copy(phase = GamePhase.PLAYING) }
    }

    private fun triggerBotTurn() {
        botJob?.cancel()
        botJob = viewModelScope.launch {
            delay(1200)
            val state = _gameState.value ?: return@launch
            if (state.activePlayerIndex != 1 || !state.player2.isBot) return@launch

            // Bot tries to play a card
            val playable = state.player2.hand.filter { it.cost <= state.player2.energy }
            if (playable.isNotEmpty()) {
                val cardToPlay = playable.maxByOrNull { it.cost } ?: playable.first()
                _selectedHandCard.value = cardToPlay
                playSelectedCard()
                delay(800)
            }

            // Bot attacks with ready creatures
            val readyCreatures = _gameState.value?.player2?.field?.filter { it.canAttack } ?: emptyList()
            for (c in readyCreatures) {
                _selectedAttacker.value = c
                val p1Taunt = _gameState.value?.player1?.field?.firstOrNull { it.hasTaunt }
                attackWithSelected(p1Taunt)
                delay(700)
            }

            delay(500)
            endTurn()
        }
    }

    private fun checkGameOver() {
        val state = _gameState.value ?: return
        if (state.player1.health <= 0) {
            endMatch(winner = state.player2, loser = state.player1, reason = "KO")
        } else if (state.player2.health <= 0) {
            endMatch(winner = state.player1, loser = state.player2, reason = "KO")
        }
    }

    fun surrender() {
        val state = _gameState.value ?: return
        val current = state.activePlayer
        val opponent = state.waitingPlayer
        endMatch(winner = opponent, loser = current, reason = "SURRENDER")
    }

    private fun endMatch(winner: Player, loser: Player, reason: String) {
        timerJob?.cancel()
        botJob?.cancel()
        val state = _gameState.value ?: return

        val winLogs = if (!winner.isBot) {
            listOf(
                "🏆 ${winner.name} Menang! ($reason)",
                "💬 ini lebih seru dari pada judi mainini aja ok?"
            )
        } else {
            listOf("🏆 ${winner.name} Menang! ($reason)")
        }

        _gameState.update {
            it?.copy(
                phase = GamePhase.GAME_OVER,
                winnerName = winner.name,
                isWinnerBot = winner.isBot,
                gameOverReason = reason,
                logs = it.logs + winLogs
            )
        }

        viewModelScope.launch {
            repository.recordMatch(
                winnerUsername = winner.name,
                loserUsername = if (loser.isBot) null else loser.name,
                rounds = state.roundNumber,
                afkSwitchesCount = state.player1.afkSwitches + state.player2.afkSwitches,
                reason = reason
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        botJob?.cancel()
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameViewModel(repository) as T
        }
    }
}
