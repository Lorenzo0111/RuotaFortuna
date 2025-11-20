package game.client;

import game.Game;
import game.GamePlayer;
import game.packets.GamePacket;
import game.packets.server.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Classe del client per giocare. Contiene tutte le funzioni necessarie alla gestione del ciclo di gioco
 * Può essere gestita da una gui con {@link game.client.ui.ClientGUI ClientGUI} oppure implementando una CLI manualmente
 */
public class Client {
    private final String ip;
    private final int port;
    private final Game game = new Game(false);
    private UUID playerId;
    private Consumer<GamePacket> updateHandler;
    private Socket socket;
    private BufferedWriter writer;
    private ClientReader reader;

    /**
     * Crea un'istanza del client
     *
     * @param ip   L'ip del server
     * @param port La porta del server
     */
    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * Connette il client al server
     *
     * @return true se la connessione è riuscita
     */
    public boolean connect() {
        try {
            socket = new Socket(ip, port);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new ClientReader(this, socket);

            reader.start();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Invia un pacchetto al server
     *
     * @param packet Il pacchetto da inviare
     * @throws IOException Se c'è stato un errore durante l'invio
     */
    public void write(GamePacket packet) throws IOException {
        writer.write(packet.serialize() + '\n');
        writer.flush();
    }

    /**
     * Gestisce la ricezione di un messaggio
     *
     * @return L'istanza del pacchetto ricevuto
     */
    public GamePacket onMessage(String message) {
        GamePacket packet = GamePacket.from(message);

        if (packet instanceof AuthSuccessPacket successPacket)
            this.playerId = successPacket.getUniqueId();

        if (packet instanceof UpdatePacket updatePacket)
            game.deserialize(updatePacket.getSerialized());

        if (packet instanceof PlayerUpdatePacket updatePacket) {
            GamePlayer target = game.getPlayerById(updatePacket.getUniqueId());
            if (target == null) {
                target = new Player(updatePacket.getUniqueId(), updatePacket.getUsername());
                game.getPlayers().add(target);
            }

            target.setUsername(updatePacket.getUsername());
            target.setPoints(updatePacket.getPoints());
            target.setMatchPoints(updatePacket.getMatchPoints());
            target.setLastGuess(updatePacket.getLastGuess());
        }

        if (packet instanceof PlayerRemovePacket updatePacket) {
            GamePlayer target = game.getPlayerById(updatePacket.getUniqueId());
            if (target == null) return packet;

            game.getPlayers().remove(target);
        }

        if (packet instanceof WinPacket || packet instanceof LosePacket)
            game.setStarted(false);

        if (packet instanceof NextRoundPacket nextRoundPacket)
            game.setCurrentRound(nextRoundPacket.getRound());

        return packet;
    }

    /**
     * Gestisce l'aggiornamento dell'interfaccia utilizzando l'update handler impostato
     *
     * @param packet L'aggiornamento da gestire
     */
    public void handleUpdate(GamePacket packet) {
        if (updateHandler != null) updateHandler.accept(packet);
    }

    /**
     * Chiude la connessione al client
     *
     * @throws IOException Se c'è stato un'errore durante la chiusura
     */
    public void close() throws IOException {
        socket.close();
        reader.close();
        writer.close();
    }

    public Game getGame() {
        return game;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setUpdateHandler(Consumer<GamePacket> updateHandler) {
        this.updateHandler = updateHandler;
    }
}