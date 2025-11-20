package game.packets.server;

import game.GamePlayer;
import game.packets.GamePacket;

import java.util.UUID;

/**
 * Rappresenta un aggiornamento di un nuovo o esistente giocatore della partita.
 * Come dati contiene:
 * <ul>
 *     <li>Nome utente</li>
 *     <li>L'{@link UUID}</li>
 *     <li>I punti totali dell'utente</li>
 *     <li>I punti totali dell'utente</li>
 *     <li>L'ultimo tentativo dell'utente</li>
 * </ul>
 * Inviato dal server al client
 */
public class PlayerUpdatePacket extends GamePacket {
    private UUID uuid;
    private String username;
    private int points;
    private int matchPoints;
    private String lastGuess;

    /**
     * Inizializza il pacchetto vuoto per la deserializzazione
     */
    public PlayerUpdatePacket() {
        super("PLAYER_UPDATE");
    }

    /**
     * Inizializza il pacchetto con le informazioni del giocatore per l'invio
     */
    public PlayerUpdatePacket(GamePlayer player) {
        this();

        this.uuid = player.getUniqueId();
        this.username = player.getUsername();
        this.points = player.getPoints();
        this.matchPoints = player.getMatchPoints();
        this.lastGuess = player.getLastGuess();
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public int getPoints() {
        return points;
    }

    public int getMatchPoints() {
        return matchPoints;
    }

    public String getLastGuess() {
        return lastGuess;
    }

    @Override
    public String serialize() {
        return formatString(
                uuid.toString(),
                username,
                String.valueOf(points),
                String.valueOf(matchPoints),
                lastGuess == null ? "" : lastGuess
        );
    }

    @Override
    public void deserialize(String serialized) {
        String[] split = serialized.split("\\|");
        if (split.length < 5)
            throw new IllegalArgumentException("La stringa serializzata " + serialized + " non è valida");

        this.uuid = UUID.fromString(split[1]);
        this.username = split[2];
        this.points = Integer.parseInt(split[3]);
        this.matchPoints = Integer.parseInt(split[4]);
        this.lastGuess = split.length > 5 ? split[5] : "";
    }
}
