package game.packets.server;

import game.packets.GamePacket;

/**
 * Rappresenta un aggiornamento dello stato di gioco. Come dati contiene il gioco serializzato
 * Inviato dal server al client
 */
public class UpdatePacket extends GamePacket {
    private String serialized;

    /**
     * Inizializza il pacchetto
     * @param id L'id diverso per eventuali override
     */
    public UpdatePacket(String id) {
        super(id);
    }

    /**
     * Inizializza il pacchetto vuoto
     */
    public UpdatePacket() {
        this("UPDATE");
    }

    /**
     * Imposta il valore del gioco serializzato
     * @param serialized Il gioco serializato
     * @return L'istanza del pacchetto
     */
    public UpdatePacket setSerialized(String serialized) {
        this.serialized = serialized;
        return this;
    }

    /**
     * @return L'istanza del gioco serializato. Da non confondere con {@link UpdatePacket#serialize}
     */
    public String getSerialized() {
        return serialized;
    }

    @Override
    public String serialize() {
        return formatString(serialized);
    }

    @Override
    public void deserialize(String serialized) {
        String[] split = serialized.split("\\|", 2);
        if (split.length != 2)
            throw new IllegalArgumentException("La stringa serializzata " + serialized + " non è valida");

        this.serialized = split[1];
    }
}
