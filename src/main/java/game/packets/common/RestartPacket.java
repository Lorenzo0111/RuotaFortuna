package game.packets.common;

import game.packets.GamePacket;

/**
 * Rappresenta una richiesta per il server di iniziare una nuova partita
 */
public class RestartPacket extends GamePacket {

    /**
     * Inizializza il pacchetto
     */
    public RestartPacket() {
        super("RESTART");
    }

    @Override
    public String serialize() {
        return formatString();
    }

    @Override
    public void deserialize(String serialized) {

    }
}
