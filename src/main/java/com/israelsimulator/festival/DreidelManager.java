package com.israelsimulator.festival;

import java.util.Random;
import java.util.UUID;

/**
 * Dreidel mini-game and spinning logic for Hanukkah (GAME_DESIGN.md §34, TODO §34).
 *
 * <p>Each spin stakes {@link #STAKE} Shekels. The payout of a letter is the <em>net</em> change
 * (stake already accounted for): Nun 0, Gimel +2, Hei +1, Shin -3 (the whole stake is lost).
 * With four equally likely faces the expected value is exactly 0, so the dreidel is a fair
 * game and can no longer be farmed for free Shekels.</p>
 */
public final class DreidelManager {

    private static final Random RANDOM = new Random();

    /** Shekels staked on every spin. */
    public static final int STAKE = 3;
    /** Item cooldown after a spin: 10 s. */
    public static final int COOLDOWN_TICKS = 200;

    public enum DreidelLetter {
        NUN("nun", "Nes (Miracle) - Pass! You keep your stake.", 0),
        GIMEL("gimel", "Gadol (Great) - Win! Take the pot: +2 Shekels!", 2),
        HEI("hei", "Haya (Happened) - Half! Take half the pot: +1 Shekel!", 1),
        SHIN("shin", "Sham (There) - Put in! Your stake goes into the pot.", -3);

        private final String id;
        private final String meaning;
        private final int shekelPayout;

        DreidelLetter(String id, String meaning, int shekelPayout) {
            this.id = id;
            this.meaning = meaning;
            this.shekelPayout = shekelPayout;
        }

        public String getId() {
            return id;
        }

        public String getMeaning() {
            return meaning;
        }

        public int getShekelPayout() {
            return shekelPayout;
        }
    }

    public record SpinResult(DreidelLetter letter, int payout) {}

    private DreidelManager() {}

    /** Shekels handed back after a spin (stake + net payout, never negative). */
    public static int grossReturn(DreidelLetter letter) {
        return Math.max(0, STAKE + letter.getShekelPayout());
    }

    /** Expected net Shekels per spin with a fair four-sided dreidel. */
    public static double expectedNetPayout() {
        double sum = 0;
        for (DreidelLetter l : DreidelLetter.values()) {
            sum += l.getShekelPayout();
        }
        return sum / DreidelLetter.values().length;
    }

    /**
     * Spins the dreidel and returns the landing letter and payout result.
     */
    public static SpinResult spin(UUID playerId) {
        DreidelLetter[] letters = DreidelLetter.values();
        DreidelLetter landed = letters[RANDOM.nextInt(letters.length)];
        return new SpinResult(landed, landed.getShekelPayout());
    }

    public static SpinResult spinDeterministic(int index) {
        DreidelLetter[] letters = DreidelLetter.values();
        DreidelLetter landed = letters[Math.abs(index) % letters.length];
        return new SpinResult(landed, landed.getShekelPayout());
    }
}
