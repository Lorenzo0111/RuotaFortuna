package game.packets.client;

import game.packets.GamePacket;

/**
 * Rappresenta l'invio al server del nome utente del giocatore
 */
public class AuthPacket extends GamePacket {
    private String username;

    /**
     * Inizializza il pacchetto vuoto per la deserializzazione
     */
    public AuthPacket() {
        super("AUTH");
    }

    /**
     * Inizializza il pacchetto
     */
    public AuthPacket(String username) {
        this();

        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String serialize() {
        return formatString(username);
    }

    @Override
    public void deserialize(String serialized) {
        String[] split = serialized.split("\\|");
        if (split.length != 2)
            throw new IllegalArgumentException("La stringa serializzata " + serialized + " non è valida");

        this.username = split[1];
    }
}
