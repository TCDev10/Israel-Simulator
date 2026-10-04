package com.israelsimulator.quest;

import com.israelsimulator.economy.CurrencyUnit;
import com.israelsimulator.reputation.ReputationFaction;

/**
 * Defines optional, discovery-driven quest milestones (GAME_DESIGN.md §52).
 * These are completely non-linear and designed to guide player discovery without railroading.
 */
public enum DiscoveryQuest {
    HOLY_CITY_PILGRIM(
            "Holy City Pilgrim",
            "Visit the Western Wall in Jerusalem and offer a prayer note.",
            QuestCategory.CULTURAL,
            ReputationFaction.RELIGIOUS,
            15,
            CurrencyUnit.SHEKEL,
            10
    ),
    MEDITERRANEAN_EXPLORER(
            "Mediterranean Explorer",
            "Cycle through Tel Aviv and explore the historic Jaffa port.",
            QuestCategory.EXPLORATION,
            ReputationFaction.CITY,
            10,
            CurrencyUnit.SHEKEL,
            8
    ),
    DEAD_SEA_MINERALIST(
            "Dead Sea Mineralist",
            "Harvest therapeutic mud and salt crystals from the Dead Sea.",
            QuestCategory.EXPLORATION,
            ReputationFaction.MERCHANT,
            10,
            CurrencyUnit.SHEKEL,
            12
    ),
    MARKET_ENTREPRENEUR(
            "Market Entrepreneur",
            "Engage in fair trade with Levantine food merchants and earn Shekels.",
            QuestCategory.COMMERCE,
            ReputationFaction.MERCHANT,
            15,
            CurrencyUnit.SHEKEL,
            15
    ),
    SILICON_ALLEY_PIONEER(
            "Silicon Alley Pioneer",
            "Craft or trade for high-tech components in the tech district.",
            QuestCategory.TECHNOLOGY,
            ReputationFaction.TECH_DISTRICT,
            20,
            CurrencyUnit.SHEKEL,
            20
    ),
    CIVIC_VOICE(
            "Civic Voice",
            "Participate attentively in a public assembly or forum speech.",
            QuestCategory.CHALLENGE,
            ReputationFaction.CITY,
            20,
            CurrencyUnit.SHEKEL,
            16
    ),
    DEFENDER_OF_THE_REALM(
            "Defender of the Realm",
            "Confront the legendary Bibi Boss and retrieve the Hava Nagila music disc.",
            QuestCategory.CHALLENGE,
            ReputationFaction.MERCHANT,
            30,
            CurrencyUnit.SHEKEL,
            48
    );

    private final String title;
    private final String description;
    private final QuestCategory category;
    private final ReputationFaction rewardFaction;
    private final int reputationReward;
    private final CurrencyUnit rewardCurrency;
    private final int rewardAmount;

    DiscoveryQuest(String title, String description, QuestCategory category,
                   ReputationFaction rewardFaction, int reputationReward,
                   CurrencyUnit rewardCurrency, int rewardAmount) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.rewardFaction = rewardFaction;
        this.reputationReward = reputationReward;
        this.rewardCurrency = rewardCurrency;
        this.rewardAmount = rewardAmount;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public QuestCategory getCategory() {
        return category;
    }

    public ReputationFaction getRewardFaction() {
        return rewardFaction;
    }

    public int getReputationReward() {
        return reputationReward;
    }

    public CurrencyUnit getRewardCurrency() {
        return rewardCurrency;
    }

    public int getRewardAmount() {
        return rewardAmount;
    }
}
