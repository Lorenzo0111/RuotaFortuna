package game.packets.server;

import game.packets.GamePacket;

import java.util.UUID;

/**
 * Rappresenta l'invio al client dell'id univoco del giocatore stesso
 */
public class AuthSuccessPacket extends GamePacket {
    private UUID uuid;

    /**
     * Inizializza il pacchetto vuoto per la deserializzazione
     */
    public AuthSuccessPacket() {
        super("AUTH_SUCCESS");
    }

    /**
     * Inizializza il pacchetto
     */
    public AuthSuccessPacket(UUID uuid) {
        this();

        this.uuid = uuid;
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
