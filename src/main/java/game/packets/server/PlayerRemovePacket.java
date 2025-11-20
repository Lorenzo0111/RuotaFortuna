package game.packets.server;

import game.GamePlayer;
import game.packets.GamePacket;

import java.util.UUID;

/**
 * Rappresenta la disconnessione di un giocatore dalla partita.
 * Come dati contiene l'UUID dell'utente
 * Inviato dal server al client
 */
public class PlayerRemovePacket extends GamePacket {
    private UUID uuid;

    /**
     * Inizializza il pacchetto vuoto per la deserializzazione
     */
    public PlayerRemovePacket() {
        super("PLAYER_REMOVE");
    }

    /**
     * Inizializza il pacchetto con le informazioni del giocatore per l'invio
     */
    public PlayerRemovePacket(GamePlayer player) {
        this();

        this.uuid = player.getUniqueId();
    }

    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public String serialize() {
        return formatString(uuid.toString());
    }

    @Override
    public void deserialize(String serialized) {
        String[] split = serialized.split("\\|");
        if (split.length != 2)
            throw new IllegalArgumentException("La stringa serializzata " + serialized + " non è valida");

        this.uuid = UUID.fromString(split[1]);
    }
}
