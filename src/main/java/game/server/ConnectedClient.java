package game.server;

import game.Game;
import game.GamePlayer;
import game.Round;
import game.packets.GamePacket;
import game.packets.client.AuthPacket;
import game.packets.client.TryPacket;
import game.packets.common.RestartPacket;
import game.packets.common.SpinPacket;
import game.packets.server.*;

import java.io.*;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rappresenta l'istanza del client connesso al server da parte del server.
 * Contiene tutte le funzioni per la gestione della connessione e della comunicazione
 */
public class ConnectedClient extends GamePlayer implements Runnable {
    private final Server server;
    private final Socket client;
    private final BufferedReader inputStream;
    private final BufferedWriter outputStream;
    private Thread thread;

    /**
     * Crea un'istanza del client
     *
     * @param server L'istanza del server
     * @param client Il socket della connessione
     * @throws IOException Se c'è un errore durante l'apertura dei canali di comunicazione
     */
    public ConnectedClient(Server server, Socket client) throws IOException {
        super(UUID.randomUUID());
        this.server = server;
        this.client = client;

        inputStream = new BufferedReader(new InputStreamReader(client.getInputStream()));
        outputStream = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
    }

    /**
     * Avvia il thread che legge i dati dal client e li processa
     */
    public void start() {
        if (thread != null && thread.isAlive()) return;

        thread = new Thread(this);
        thread.start();
    }

    /**
     * Legge l'input dal client. Blocca il processo finché non vengono inviati dati
     *
     * @return La stringa inviata dal client
     * @throws IOException Se c'è stato un errore durante la lettura
     */
    private String readInput() throws IOException {
        return inputStream.readLine();
    }

    /**
     * Invia un {@link GamePacket} al client
     *
     * @param packet Il pacchetto da inviare
     */
    public void write(GamePacket packet) {
        try {
            outputStream.write(packet.serialize() + "\n");
            outputStream.flush();
        } catch (IOException e) {
            // Il client si è disconnesso
            this.close();
        }
    }

    /**
     * Chiude la connessione al client
     */
    public void close() {
        try {
            this.client.close();
            this.inputStream.close();
            this.outputStream.close();

            if (thread != null) {
                this.thread.interrupt();
                thread = null;
            }
        } catch (IOException e) {
            // Il client è già chiuso
        }

        server.getGame().getPlayers().remove(this);
        server.broadcast(new PlayerRemovePacket(this));
    }

    @Override
    public void run() {
        try {
            Game game = server.getGame();

            // Finché il client è connesso
            while (client.isConnected()) {
                // Legge input dal client
                String input = this.readInput();
                if (input == null || input.isEmpty()) continue;

                GamePacket packet = GamePacket.from(input);

                switch (packet) {
                    case AuthPacket authPacket -> {
                        String username = authPacket.getUsername().trim();

                        if (username.length() < 3) {
                            this.write(new GameErrorPacket(GameErrorPacket.ErrorType.INVALID_USERNAME));
                            continue;
                        }

                        if (game.getPlayers().stream().anyMatch(player -> player.getUsername().equalsIgnoreCase(username))) {
                            this.write(new GameErrorPacket(GameErrorPacket.ErrorType.USED_USERNAME));
                            continue;
                        }

                        this.setUsername(username);
                        this.write(new AuthSuccessPacket(this.getUniqueId()));
                        server.broadcast(new PlayerUpdatePacket(this));
                    }
                    case RestartPacket ignored -> {
                        game.start();
                        game.getPlayers().forEach(player -> server.broadcast(new PlayerUpdatePacket(player)));
                        server.broadcast(new UpdatePacket().setSerialized(game.serialize()));
                        server.broadcast(new RestartPacket());
                        game.nextRound(true, server::broadcast);
                    }
                    case SpinPacket ignored -> {
                        // Verifica che sia il turno del giocatore e che il bonus non sia stato ancora determinato
                        if (game.getCurrentRound() == null || !game.getCurrentRound().getPlayer().equals(this)) {
                            System.out.println("ATTENZIONE! " + getUsername() + " ha provato a girare la ruota fuori dal turno");
                            continue;
                        }

                        if (game.getCurrentRound().getBonus() != null) {
                            System.out.println("ATTENZIONE! " + getUsername() + " ha provato a girare la ruota quando il bonus è già stato determinato");
                            continue;
                        }

                        // Calcola il bonus. Default a 100
                        Round.Bonus selectedBonus = Round.Bonus.M100;

                        // Valore casuale sulla somma dei pesi
                        int randomValue = ThreadLocalRandom.current().nextInt(Round.Bonus.getTotal());

                        // Somma del peso progressiva
                        int sum = 0;
                        // Per tutti i bonus
                        for (Round.Bonus bonus : Round.Bonus.values()) {
                            // Aggiungi il peso
                            sum += bonus.getWeight();
                            // Se il valore casuale è minore della somma, questo bonus è stato selezionato
                            if (randomValue < sum) {
                                selectedBonus = bonus;
                                break;
                            }
                        }

                        game.getCurrentRound().setBonus(selectedBonus);

                        // Invia il risultato a tutti i client
                        server.broadcast(new NextRoundPacket(game.getCurrentRound()));

                        // Se il bonus è SALTA o BANCAROTTA, passa automaticamente al turno successivo
                        if (selectedBonus.equals(Round.Bonus.SALTA) || selectedBonus.equals(Round.Bonus.BANCAROTTA)) {
                            if (selectedBonus.equals(Round.Bonus.BANCAROTTA)) {
                                this.setMatchPoints(0);
                                this.setPoints(0);
                            }

                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException ignored2) {
                            }

                            server.broadcast(new PlayerUpdatePacket(this));
                            game.nextRound(true, server::broadcast);
                        }
                    }
                    case TryPacket tryPacket -> {
                        if (game.getCurrentRound() == null || !game.getCurrentRound().getPlayer().equals(this)) {
                            System.out.println("ATTENZIONE! " + getUsername() + " ha provato a indovinare fuori dal turno");
                            continue;
                        }

                        if (game.getCurrentRound().getBonus() == null) {
                            System.out.println("ATTENZIONE! " + getUsername() + " ha provato a indovinare prima di girare la ruota");
                            continue;
                        }

                        String tentativo = tryPacket.getTentativo();
                        this.setLastGuess(tentativo);

                        if (tentativo.length() == 1) {
                            // Se l'input è una sola lettera, il client sta provando a indovinare una lettera

                            if (game.guessChar(tentativo.charAt(0))) {
                                // Se ha indovinato tutte le lettere
                                if (game.isComplete()) {
                                    game.setStarted(false);

                                    server.broadcast(new WinPacket(this.getUniqueId(), game.getSolution()));
                                    server.broadcast(new PlayerUpdatePacket(this));
                                    continue;
                                }

                                server.broadcast(new PlayerUpdatePacket(this));
                                server.broadcast(new UpdatePacket().setSerialized(game.serialize()));

                                game.nextRound(false, server::broadcast);
                            } else {
                                // Se l'utente ha finito le vite
                                if (game.getLives() <= 0) {
                                    server.broadcast(new LosePacket(game.getSolution()));
                                    game.setStarted(false);
                                    continue;
                                }

                                server.broadcast(new PlayerUpdatePacket(this));
                                this.write(new ErrorPacket().setSerialized(game.serialize()));
                                server.broadcast(new UpdatePacket().setSerialized(game.serialize()));

                                game.nextRound(true, server::broadcast);
                            }
                        } else {
                            // Altrimenti il client sta provando a indovinare l'intera frase
                            boolean result = game.guessString(tentativo);
                            if (result) {
                                // Il client ha vinto
                                game.setStarted(false);

                                server.broadcast(new WinPacket(this.getUniqueId(), game.getSolution()));
                                server.broadcast(new PlayerUpdatePacket(this));
                                continue;
                            }

                            // Se sono finite le vite del gioco
                            if (game.getLives() <= 0) {
                                server.broadcast(new LosePacket(game.getSolution()));
                                game.setStarted(false);
                                continue;
                            }

                            server.broadcast(new PlayerUpdatePacket(this));
                            this.write(new ErrorPacket().setSerialized(game.serialize()));
                            server.broadcast(new UpdatePacket().setSerialized(game.serialize()));
                            game.nextRound(true, server::broadcast);
                        }
                    }
                    default -> {
                    }
                }
            }
        } catch (IOException e) {
            // Il socket si è disconnesso
            this.close();
        }
    }

}
