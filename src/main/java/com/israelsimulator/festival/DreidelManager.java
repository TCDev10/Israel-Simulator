package com.israelsimulator.festival;

import java.util.Random;
import java.util.UUID;

/**
 * Dreidel mini-game and spinning logic for Hanukkah (GAME_DESIGN.md §34, TODO §34).
 */
public final class DreidelManager {

    private static final Random RANDOM = new Random();

    public enum DreidelLetter {
        NUN("nun", "Nes (Miracle) - Pass! Nothing happens.", 0),
        GIMEL("gimel", "Gadol (Great) - Win! Take the whole pot!", 5),
        HEI("hei", "Haya (Happened) - Half! Take half the pot!", 2),
        SHIN("shin", "Sham (There) - Put in! Place 1 into the pot.", -1);

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
