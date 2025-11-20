package game.packets.server;

import game.packets.GamePacket;

/**
 * Rappresenta il pacchetto di sconfitta. Contiene la soluzione
 */
public class LosePacket extends GamePacket {
    private String soluzione;

    /**
     * Inizializza il pacchetto vuoto per la deserializzazione
     */
    public LosePacket() {
        super("LOSE");
    }

    /**
     * Inizializza il pacchetto con la soluzione per l'invio
     */
    public LosePacket(String soluzione) {
        this();

        this.soluzione = soluzione;
    }

    public String getSoluzione() {
        return soluzione;
    }

    @Override
    public String serialize() {
        return formatString(soluzione);
    }

    @Override
    public void deserialize(String serialized) {
        String[] split = serialized.split("\\|");
        if (split.length != 2)
            throw new IllegalArgumentException("La stringa serializzata " + serialized + " non è valida");

        this.soluzione = split[1];
    }
}
