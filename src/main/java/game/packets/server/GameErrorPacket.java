package game.packets.server;

import game.packets.GamePacket;

/**
 * Rappresenta il pacchetto di un errore correlato allo stato di gioco. Contiene il tipo di errore
 */
public class GameErrorPacket extends GamePacket {
    private ErrorType type;

    /**
     * Inizializza il pacchetto vuoto per la deserializzazione
     */
    public GameErrorPacket() {
        super("GAME_ERROR");
    }

    /**
     * Inizializza il pacchetto con la soluzione per l'invio
     */
    public GameErrorPacket(ErrorType type) {
        this();

        this.type = type;
    }

    public ErrorType getType() {
        return type;
    }

    @Override
    public String serialize() {
        return formatString(type.name());
    }

    @Override
    public void deserialize(String serialized) {
        String[] split = serialized.split("\\|");
        if (split.length != 2)
            throw new IllegalArgumentException("La stringa serializzata " + serialized + " non è valida");

        this.type = ErrorType.valueOf(split[1]);
    }

    /**
     * Rappresenta il tipo di errore
     */
    public enum ErrorType {
        /**
         * L'errore notifica l'utente che il gioco è gia in corso. Utile quando un giocatore prova a entrare durante
         * una partita
         */
        GAME_RUNNING("Una partita è già in corso, riprova più tardi", true),

        /**
         * L'errore notifica l'utente che il nome utente inserito non è valido
         */
        INVALID_USERNAME("Il nome utente inserito non è valido. Assicurati che abbia almeno 3 caratteri", false),

        /**
         * L'errore notifica l'utente che il nome utente inserito è già utilizzato
         */
        USED_USERNAME("Il nome utente inserito è già stato utilizzato da un altro giocatore", false);

        private final String message;
        private final boolean disconnect;

        /**
         * @param message    Il messaggio come stringa
         * @param disconnect True se il client deve disconnettersi dopo aver ricevuto questo errore
         */
        ErrorType(String message, boolean disconnect) {
            this.message = message;
            this.disconnect = disconnect;
        }

        public String getMessage() {
            return message;
        }

        public boolean shouldDisconnect() {
            return disconnect;
        }
    }
}
