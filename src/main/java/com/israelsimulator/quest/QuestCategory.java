package com.israelsimulator.quest;

/**
 * Categories of non-linear quests and discovery tracks.
 */
public enum QuestCategory {
    EXPLORATION("Regional Exploration"),
    CULTURAL("Sacred & Cultural Heritage"),
    COMMERCE("Market & Economy"),
    TECHNOLOGY("High-Tech & Innovation"),
    CHALLENGE("Heroic Challenges");

    private final String displayName;

    QuestCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
