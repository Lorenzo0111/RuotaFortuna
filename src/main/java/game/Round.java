package game;

import java.util.Objects;

/**
 * Classe rappresentante il turno di un giocatore
 */
public final class Round {
    private final GamePlayer player;
    private Bonus bonus = null;

    /**
     * @param player L'id del giocatore
     */
    public Round(GamePlayer player) {
        this.player = player;
    }

    public GamePlayer getPlayer() {
        return player;
    }

    public Bonus getBonus() {
        return bonus;
    }

    public void setBonus(Bonus bonus) {
        this.bonus = bonus;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Round) obj;
        return Objects.equals(this.player, that.player) &&
                Objects.equals(this.bonus, that.bonus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, bonus);
    }

    @Override
    public String toString() {
        return "Round[" +
                "player=" + player + ", " +
                "bonus=" + bonus + ']';
    }

    /**
     * Rappresenta un bonus della ruota
     */
    public enum Bonus {
        M500(2, 500, "Bonus: +500", "500"),
        M200(3, 200, "Bonus: +200", "200"),
        M100(4, 100, "Bonus: +100", "100"),
        SALTA(2, 0, "Bonus: Salta turno", "SALTA"),
        BANCAROTTA(1, 0, "Bonus: Bancarotta", "KO");

        private static int total = 0;
        private final int weight;
        private final int multiplier;
        private final String longText;
        private final String shortText;

        static {
            for (Bonus bonus : values()) {
                total += bonus.getWeight();
            }
        }

        /**
         * @param weight Peso per la chance dell'elemento
         * @param multiplier Il numero che verrà moltiplicato alle lettere indovinate
         * @param longText Il testo da mostrare quando viene selezionato
         * @param shortText Il testo da mostrare nella ruota
         */
        Bonus(int weight, int multiplier, String longText, String shortText) {
            this.weight = weight;
            this.multiplier = multiplier;
            this.longText = longText;
            this.shortText = shortText;
        }

        public int getWeight() {
            return weight;
        }

        public int getMultiplier() {
            return multiplier;
        }

        public static int getTotal() {
            return total;
        }

        public String getLongText() {
            return longText;
        }

        public String getShortText() {
            return shortText;
        }
    }

}
