package game.packets;

import game.packets.client.AuthPacket;
import game.packets.client.TryPacket;
import game.packets.common.RestartPacket;
import game.packets.common.SpinPacket;
import game.packets.server.*;

/**
 * Rappresenta un pacchetto inviato o ricevuto
 */
public abstract class GamePacket {
    protected final String id;

    /**
     * Inizializza l'istanza del pacchetto
     *
     * @param id L'id univoco per ricondurlo alla sua classe
     */
    protected GamePacket(String id) {
        this.id = id;
    }

    /**
     * @return L'id univoco del pacchetto
     */
    public String getId() {
        return id;
    }

    /**
     * Serializza il pacchetto per l'invio
     *
     * @return La stringa serializzata
     */
    public abstract String serialize();

    /**
     * Deserializza un pacchetto ricevuto
     *
     * @param serialized La stringa serializzata con {@link GamePacket#serialize()}
     */
    public abstract void deserialize(String serialized);

    /**
     * Prepara la stringa per la serializzazione con id e argomenti
     *
     * @return La stringa serializzata con gli argomenti separati da "|"
     */
    protected String formatString(String... data) {
        StringBuilder builder = new StringBuilder(id.toUpperCase() + "|");

        for (String d : data) {
            if (d != null) builder.append(d).append("|");
        }

        return builder.substring(0, builder.length() - 1);
    }

    @Override
    public String toString() {
        return this.serialize();
    }

    /**
     * Deserializza il pacchetto inviato nella relativa istanza
     *
     * @param serialized La stringa serializzata con {@link GamePacket#serialize()}
     * @return Il pacchetto deserializzato
     */
    public static GamePacket from(String serialized) {
        String[] split = serialized.split("\\|");

        GamePacket packet = switch (split[0].toUpperCase()) {
            case "AUTH" -> new AuthPacket();
            case "TRY" -> new TryPacket();
            case "RESTART" -> new RestartPacket();
            case "SPIN" -> new SpinPacket();
            case "AUTH_SUCCESS" -> new AuthSuccessPacket();
            case "ERROR" -> new ErrorPacket();
            case "GAME_ERROR" -> new GameErrorPacket();
            case "LOSE" -> new LosePacket();
            case "NEXT_ROUND" -> new NextRoundPacket();
            case "PLAYER_REMOVE" -> new PlayerRemovePacket();
            case "PLAYER_UPDATE" -> new PlayerUpdatePacket();
            case "UPDATE" -> new UpdatePacket();
            case "WIN" -> new WinPacket();
            default -> throw new IllegalArgumentException("Tipo di pacchetto " + split[0] + " sconosciuto");
        };

        packet.deserialize(serialized);

        return packet;
    }
}
