package game.client;

import game.packets.GamePacket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Thread per la gestione della lettura dei messaggi ricevuti dal server
 */
public class ClientReader implements Runnable {
    private final Client client;
    private final Socket socket;
    private final BufferedReader inputStream;
    private Thread thread;

    /**
     * Crea un'istanza del lettore
     *
     * @param client L'istanza del client
     * @param socket Il socket della connessione
     * @throws IOException Se c'è un errore durante l'apertura dei canali di comunicazione
     */
    public ClientReader(Client client, Socket socket) throws IOException {
        this.client = client;
        this.socket = socket;

        inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * Avvia il thread che legge i dati dal server e li processa
     */
    public void start() {
        if (thread != null && thread.isAlive()) return;

        thread = new Thread(this);
        thread.start();
    }

    /**
     * Legge l'output dal server. Blocca il processo finché non vengono inviati dati
     *
     * @return La stringa inviata dal server
     * @throws IOException Se c'è stato un errore durante la lettura
     */
    public String read() throws IOException {
        return inputStream.readLine();
    }

    /**
     * Chiude la connessione al server
     */
    public void close() {
        try {
            if (thread != null) {
                this.thread.interrupt();
                thread = null;
            }

            this.inputStream.close();
        } catch (IOException e) {
            // Il client è già chiuso
        }
    }

    @Override
    public void run() {
        try {
            // Finché il client è connesso
            while (socket.isConnected()) {
                // Attendi l'OK dal server iniziale o il successivo aggiornamento di dati
                String message = this.read();

                // Gestisci il messaggio
                GamePacket packet = client.onMessage(message);

                // Aggiorna l'interfaccia
                client.handleUpdate(packet);
            }
        } catch (IOException e) {
            // Il socket si è disconnesso
            this.close();
        }
    }
}
