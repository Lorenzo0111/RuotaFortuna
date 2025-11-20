package game.packets.common;

import game.Round;
import game.packets.GamePacket;

/**
 * Rappresenta una richiesta di girare la ruota o il risultato quando inviato dal server con un risultato
 */
public class SpinPacket extends GamePacket {
    private Round.Bonus result = null;

    /**
     * Inizializza il pacchetto vuoto per la deserializzazione
     */
    public SpinPacket() {
        super("SPIN");
    }

    /**
     * Inizializza il pacchetto con il risultato quando inviato dal server
     */
    public SpinPacket(Round.Bonus result) {
        this();

        this.result = result;
    }

    public Round.Bonus getResult() {
        return result;
    }

    @Override
    public String serialize() {
        return formatString(result == null ? null : result.name());
    }

    @Override
    public void deserialize(String serialized) {
        String[] split = serialized.split("\\|");
        if (split.length > 2)
            throw new IllegalArgumentException("La stringa serializzata " + serialized + " non è valida");

        this.result = split.length > 1 ? Round.Bonus.valueOf(split[1]) : null;
    }
}
