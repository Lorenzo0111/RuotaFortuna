package game.packets.server;

import game.packets.GamePacket;

import java.util.UUID;

/**
 * Rappresenta il pacchetto di vittoria. Contiene la soluzione
 */
public class WinPacket extends GamePacket {
    private UUID vincitore;
    private String soluzione;

    /**
     * Inizializza il pacchetto vuoto per la deserializzazione
     */
    public WinPacket() {
        super("WIN");
    }

    /**
     * Inizializza il pacchetto con la soluzione per l'invio
     */
    public WinPacket(UUID vincitore, String soluzione) {
        this();

        this.vincitore = vincitore;
        this.soluzione = soluzione;
    }

    public UUID getVincitore() {
        return vincitore;
    }

    public String getSoluzione() {
        return soluzione;
    }

    @Override
    public String serialize() {
        return formatString(vincitore.toString(), soluzione);
    }

    @Override
    public void deserialize(String serialized) {
        String[] split = serialized.split("\\|");
        if (split.length != 3)
            throw new IllegalArgumentException("La stringa serializzata " + serialized + " non è valida");

        this.vincitore = UUID.fromString(split[1]);
        this.soluzione = split[2];
    }
}
