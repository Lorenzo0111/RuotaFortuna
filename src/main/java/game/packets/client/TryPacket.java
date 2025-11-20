package game.packets.client;

import game.packets.GamePacket;

/**
 * Rappresenta un tentativo di indovinare una frase o una lettera
 */
public class TryPacket extends GamePacket {
    private String tentativo;

    /**
     * Inizializza il pacchetto vuoto per la deserializzazione
     */
    public TryPacket() {
        super("TRY");
    }

    /**
     * Inizializza il pacchetto con il tentativo per l'invio
     */
    public TryPacket(String tentativo) {
        this();

        this.tentativo = tentativo;
    }

    public String getTentativo() {
        return tentativo;
    }

    @Override
    public String serialize() {
        return formatString(tentativo);
    }

    @Override
    public void deserialize(String serialized) {
        String[] split = serialized.split("\\|");
        if (split.length != 2)
            throw new IllegalArgumentException("La stringa serializzata " + serialized + " non è valida");

        this.tentativo = split[1];
    }
}
