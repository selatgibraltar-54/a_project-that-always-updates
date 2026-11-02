package com.apksafe.cardgame.data.model

object CardCatalog {
    val allCards: List<Card> = listOf(
        // Fire
        Card(
            id = "fire_1",
            name = "Flame Wisp",
            description = "Quick burning spirit that attacks swiftly.",
            cost = 1,
            attack = 2,
            defense = 1,
            element = CardElement.FIRE,
            rarity = CardRarity.COMMON,
            type = CardType.CREATURE
        ),
        Card(
            id = "fire_2",
            name = "Inferno Burst",
            description = "Hurl a ball of fire dealing 3 direct damage.",
            cost = 2,
            attack = 3,
            defense = 0,
            element = CardElement.FIRE,
            rarity = CardRarity.COMMON,
            type = CardType.SPELL
        ),
        Card(
            id = "fire_3",
            name = "Fireball",
            description = "Blast target enemy with a devastating flame dealing 5 damage.",
            cost = 3,
            attack = 5,
            defense = 0,
            element = CardElement.FIRE,
            rarity = CardRarity.RARE,
            type = CardType.SPELL
        ),
        Card(
            id = "fire_4",
            name = "Magma Golem",
            description = "Molten stone titan with Taunt. Absorbs incoming attacks.",
            cost = 4,
            attack = 4,
            defense = 6,
            element = CardElement.FIRE,
            rarity = CardRarity.EPIC,
            type = CardType.CREATURE,
            hasTaunt = true
        ),
        Card(
            id = "fire_5",
            name = "Pyre Dragon",
            description = "Legendary apex predator that incinerates battlefields.",
            cost = 6,
            attack = 7,
            defense = 5,
            element = CardElement.FIRE,
            rarity = CardRarity.LEGENDARY,
            type = CardType.CREATURE
        ),

        // Water
        Card(
            id = "water_1",
            name = "Water Sprite",
            description = "Gentle spirit of the brook.",
            cost = 1,
            attack = 1,
            defense = 3,
            element = CardElement.WATER,
            rarity = CardRarity.COMMON,
            type = CardType.CREATURE
        ),
        Card(
            id = "water_2",
            name = "Tidal Surge",
            description = "Wash away wounds, restoring 5 Health to your hero.",
            cost = 2,
            attack = 0,
            defense = 5,
            element = CardElement.WATER,
            rarity = CardRarity.COMMON,
            type = CardType.SPELL
        ),
        Card(
            id = "water_3",
            name = "Coral Guardian",
            description = "Armed reef defender with Taunt.",
            cost = 3,
            attack = 2,
            defense = 5,
            element = CardElement.WATER,
            rarity = CardRarity.RARE,
            type = CardType.CREATURE,
            hasTaunt = true
        ),
        Card(
            id = "water_4",
            name = "Frost Nova",
            description = "Freeze the area, dealing 2 damage to all enemy units.",
            cost = 3,
            attack = 2,
            defense = 0,
            element = CardElement.WATER,
            rarity = CardRarity.EPIC,
            type = CardType.SPELL
        ),
        Card(
            id = "water_5",
            name = "Abyssal Leviathan",
            description = "Colossal sea beast that commands crushing ocean depths.",
            cost = 6,
            attack = 6,
            defense = 8,
            element = CardElement.WATER,
            rarity = CardRarity.LEGENDARY,
            type = CardType.CREATURE
        ),

        // Nature
        Card(
            id = "nature_1",
            name = "Forest Wolf",
            description = "Ferocious hunter pack leader.",
            cost = 2,
            attack = 3,
            defense = 2,
            element = CardElement.NATURE,
            rarity = CardRarity.COMMON,
            type = CardType.CREATURE
        ),
        Card(
            id = "nature_2",
            name = "Thorn Shield",
            description = "Grant +4 Shield and absorb incoming damage.",
            cost = 2,
            attack = 0,
            defense = 4,
            element = CardElement.NATURE,
            rarity = CardRarity.COMMON,
            type = CardType.SHIELD
        ),
        Card(
            id = "nature_3",
            name = "Bloom of Life",
            description = "Restore 6 Health and gain +2 Shield.",
            cost = 3,
            attack = 0,
            defense = 6,
            element = CardElement.NATURE,
            rarity = CardRarity.RARE,
            type = CardType.SPELL
        ),
        Card(
            id = "nature_4",
            name = "Ancient Treant",
            description = "Massive rooted ancient with impenetrable bark and Taunt.",
            cost = 5,
            attack = 3,
            defense = 9,
            element = CardElement.NATURE,
            rarity = CardRarity.EPIC,
            type = CardType.CREATURE,
            hasTaunt = true
        ),
        Card(
            id = "nature_5",
            name = "Gaia's Blessing",
            description = "Draw 2 cards and restore 4 Health.",
            cost = 4,
            attack = 0,
            defense = 4,
            element = CardElement.NATURE,
            rarity = CardRarity.LEGENDARY,
            type = CardType.SPELL
        ),

        // Lightning
        Card(
            id = "lightning_1",
            name = "Spark Imp",
            description = "Fast electric shocker.",
            cost = 1,
            attack = 2,
            defense = 1,
            element = CardElement.LIGHTNING,
            rarity = CardRarity.COMMON,
            type = CardType.CREATURE
        ),
        Card(
            id = "lightning_2",
            name = "Storm Falcon",
            description = "Swift predator of the tempest. Ready to attack immediately.",
            cost = 2,
            attack = 3,
            defense = 2,
            element = CardElement.LIGHTNING,
            rarity = CardRarity.COMMON,
            type = CardType.CREATURE,
            canAttack = true
        ),
        Card(
            id = "lightning_3",
            name = "Thunderstrike",
            description = "Smite target with 5 points of raw lightning voltage.",
            cost = 3,
            attack = 5,
            defense = 0,
            element = CardElement.LIGHTNING,
            rarity = CardRarity.RARE,
            type = CardType.SPELL
        ),
        Card(
            id = "lightning_4",
            name = "Plasma Colossus",
            description = "Armored mechanical construct supercharged with high voltage.",
            cost = 5,
            attack = 6,
            defense = 5,
            element = CardElement.LIGHTNING,
            rarity = CardRarity.EPIC,
            type = CardType.CREATURE
        ),
        Card(
            id = "lightning_5",
            name = "Chain Lightning",
            description = "Arc of electricity dealing 4 damage to player and 2 to all units.",
            cost = 5,
            attack = 4,
            defense = 0,
            element = CardElement.LIGHTNING,
            rarity = CardRarity.LEGENDARY,
            type = CardType.SPELL
        ),

        // Void
        Card(
            id = "void_1",
            name = "Shadow Assassin",
            description = "Lurks unseen, striking with lethal precision.",
            cost = 3,
            attack = 4,
            defense = 2,
            element = CardElement.VOID,
            rarity = CardRarity.RARE,
            type = CardType.CREATURE
        ),
        Card(
            id = "void_2",
            name = "Life Drain",
            description = "Siphon 4 Health from enemy and restore it to yourself.",
            cost = 3,
            attack = 4,
            defense = 4,
            element = CardElement.VOID,
            rarity = CardRarity.RARE,
            type = CardType.SPELL
        ),
        Card(
            id = "void_3",
            name = "Dread Knight",
            description = "Fearsome undead champion with heavy void armor.",
            cost = 4,
            attack = 5,
            defense = 5,
            element = CardElement.VOID,
            rarity = CardRarity.EPIC,
            type = CardType.CREATURE,
            hasTaunt = true
        ),
        Card(
            id = "void_4",
            name = "Eclipse Phantom",
            description = "Ethereal entity that disrupts opponent's life essence.",
            cost = 6,
            attack = 7,
            defense = 6,
            element = CardElement.VOID,
            rarity = CardRarity.LEGENDARY,
            type = CardType.CREATURE
        )
    )

    fun createStandardDeck(): List<Card> {
        // Return 15 shuffled cards for a fast and exciting battle
        return allCards.shuffled().take(15).map { it.copy(id = "${it.id}_${java.util.UUID.randomUUID().toString().take(5)}") }
    }
}
