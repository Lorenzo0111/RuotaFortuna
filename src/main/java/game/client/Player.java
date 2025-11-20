package game.client;

import game.GamePlayer;

import java.util.UUID;

/**
 * Rappresenta l'istanza di un client connesso al server da parte del client.
 */
public class Player extends GamePlayer {

    /**
     * Crea l'istanza del giocatore con l'UUID passato come parametro
     *
     * @param uuid     L'UUID del giocatore
     * @param username Il nome utente del giocatore
     */
    public Player(UUID uuid, String username) {
        super(uuid);

        this.setUsername(username);
    }
}
