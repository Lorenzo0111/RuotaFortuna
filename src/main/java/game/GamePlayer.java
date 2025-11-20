package game;

import java.util.UUID;

/**
 * Classe astratta che rappresenta un giocatore connesso al server
 */
public abstract class GamePlayer {
    /**
     * Nome utente di default se non è ancora stato impostato
     */
    public static final String DEFAULT_USERNAME = "Sconosciuto";
    private final UUID uuid;
    private String username;
    private int points = 0;
    private int matchPoints = 0;
    private String lastGuess;

    /**
     * Crea l'istanza del giocatore con l'UUID passato come parametro
     * @param uuid L'UUID del giocatore
     */
    protected GamePlayer(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * @return L'id univoco del giocatore
     */
    public UUID getUniqueId() {
        return uuid;
    }

    /**
     * Imposta il nome utente del giocatore
     * @param username Il nuovo nome utente
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return Il nome dell'utente o {@link GamePlayer#DEFAULT_USERNAME}
     */
    public String getUsername() {
        return username != null ? username : DEFAULT_USERNAME;
    }

    /**
     * Imposta i punti del giocatore
     * @param points I nuovi punti
     */
    public void setPoints(int points) {
        this.points = points;
    }

    /**
     * Aggiungi dei punti al giocatore
     * @param points I punti da aggiungere
     */
    public void addPoints(int points) {
        this.points += points;
    }

    /**
     * @return I punti accumulati dal giocatore
     */
    public int getPoints() {
        return points;
    }

    /**
     * Imposta i punti del giocatore accumulati durante la partita corrente
     * @param matchPoints I nuovi punti
     */
    public void setMatchPoints(int matchPoints) {
        this.matchPoints = matchPoints;
    }

    /**
     * Aggiungi al giocatore dei punti accumulati durante la partita corrente
     * @param matchPoints I punti da aggiungere
     */
    public void addMatchPoints(int matchPoints) {
        this.matchPoints += matchPoints;
    }

    /**
     * @return I punti del giocatore accumulati durante la partita corrente
     */
    public int getMatchPoints() {
        return matchPoints;
    }

    /**
     * @return L'ultimo tentativo del giocatore
     */
    public String getLastGuess() {
        return lastGuess;
    }

    /**
     * Imposta l'ultimo tentativo del giocatore
     * @param lastGuess L'ultimo tentativo del giocatore
     */
    public void setLastGuess(String lastGuess) {
        this.lastGuess = lastGuess;
    }

    @Override
    public String toString() {
        return "GamePlayer{" +
                "uuid=" + uuid +
                ", username='" + username + '\'' +
                ", points=" + points +
                ", matchPoints=" + matchPoints +
                ", lastGuess='" + lastGuess + '\'' +
                '}';
    }
}
