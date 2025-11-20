package game.packets.server;

import game.Game;
import game.Round;
import game.packets.GamePacket;

import java.util.UUID;

/**
 * Rappresenta l'aggiornamento dello stato a un nuovo turno
 * Come dati contiene:
 * <ul>
 *     <li>Il giocatore a cui spetta il turno</li>
 *     <li>Il {@link game.Round.Bonus}</li>
 * </ul>
 * Inviato dal server al client
 */
public class NextRoundPacket extends GamePacket {
    private Round round;

    /**
     * Inizializza il pacchetto vuoto per la deserializzazione
     */
    public NextRoundPacket() {
        super("NEXT_ROUND");
    }

    /**
     * Inizializza il pacchetto con le informazioni del giocatore per l'invio
     */
    public NextRoundPacket(Round round) {
        this();

        this.round = round;
    }

    public Round getRound() {
        return round;
    }

    @Override
    public String serialize() {
        return formatString(round.getPlayer().getUniqueId().toString(), round.getBonus() != null ? round.getBonus().name() : null);
    }

    @Override
    public void deserialize(String serialized) {
        String[] split = serialized.split("\\|");
        if (split.length < 2)
            throw new IllegalArgumentException("La stringa serializzata " + serialized + " non è valida");

        UUID uuid = UUID.fromString(split[1]);
        round = new Round(Game.getInstance().getPlayerById(uuid));

        if (split.length >= 3) {
            Round.Bonus bonus = Round.Bonus.valueOf(split[2]);
            round.setBonus(bonus);
        }
    }
}
