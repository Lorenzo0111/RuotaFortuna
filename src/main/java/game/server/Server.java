package game.server;

import game.Game;
import game.packets.GamePacket;
import game.packets.server.GameErrorPacket;
import game.packets.server.PlayerUpdatePacket;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Classe del server per la gestione del gioco.
 */
public class Server {
    private final ServerSocket server;
    private final Game game = new Game(true);
    private boolean running = true;

    /**
     * Crea un'istanza del server di gioco
     *
     * @param port Porta del socket server
     * @throws IOException Se c'è stato un errore nella creazione del server, ad esempio se la porta è occupata
     */
    public Server(int port) throws IOException {
        this.server = new ServerSocket(port);
    }

    /**
     * Blocca il processo finché non si connette un client
     */
    public void awaitClient() {
        try {
            ConnectedClient client = new ConnectedClient(this, server.accept());
            if (game.isStarted()) {
                client.write(new GameErrorPacket(GameErrorPacket.ErrorType.GAME_RUNNING));
                client.close();
                return;
            }

            client.start();

            game.getPlayers().forEach(player -> client.write(new PlayerUpdatePacket(player)));
            game.getPlayers().add(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Invia un pacchetto a tutti i client connessi
     *
     * @param packet Il pacchetto da inviare
     * @see ConnectedClient#write(GamePacket)
     */
    public void broadcast(GamePacket packet) {
        game.getPlayers().forEach(client -> ((ConnectedClient) client).write(packet));
    }

    /**
     * Chiude la connessione ai client e spegne il server
     */
    public void close() {
        running = false;
        game.getPlayers().forEach(player -> ((ConnectedClient) player).close());

        System.exit(0);
    }

    public boolean isRunning() {
        return running;
    }

    public Game getGame() {
        return game;
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(6789);

            while (server.isRunning()) {
                server.awaitClient();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}