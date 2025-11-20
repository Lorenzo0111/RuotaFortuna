package game;

import game.client.Client;
import game.packets.server.NextRoundPacket;
import game.server.Server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Classe per l'istanza del gioco. Questa classe viene utilzzata sia dal {@link Client} che dal {@link Server}
 * per mantenere in memoria l'andamento del gioco.
 * <p>
 * Il server mantiene l'autorità sulla gestione del gioco mandando informazioni per aggiornare lo stato del gioco
 */
public class Game {
    public static final int MAX_LIVES = 6;
    private static Game instance;
    private final Random random = new Random();
    private final boolean server;
    private final List<Character> guesses = new ArrayList<>();
    private final List<GamePlayer> players = new CopyOnWriteArrayList<>();
    private Character[] currentGuess;
    private String category;
    private String solution;
    private int lives;
    private boolean started = false;
    private Round currentRound;

    /**
     * Crea un'istanza del gestore del gioco
     *
     * @param server Vero se a instanziarlo è stato il server
     */
    public Game(boolean server) {
        this.server = server;

        instance = this;
    }

    /**
     * Funzione chiamata dal server per avviare il gioco.
     * Inizializza tutte le variabili e seleziona una frase dall'elenco salvato in file
     *
     * @throws IOException Se la lettura del file delle frasi fallisce
     */
    public void start() throws IOException {
        if (!server) throw new IllegalArgumentException("Solo il server può eseguire questa funzione");

        List<String> frasi = Files.readAllLines(Path.of("frasi.txt"), StandardCharsets.UTF_8);

        String[] extracted = frasi.get(random.nextInt(frasi.size())).toLowerCase().split(",", 2);

        this.started = true;
        this.category = extracted[0];
        this.solution = extracted[1];
        this.guesses.clear();
        this.currentGuess = new Character[solution.length()];
        this.lives = MAX_LIVES;
        this.currentRound = null;

        char[] charArray = solution.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == ' ') currentGuess[i] = ' ';
        }

        players.forEach(player -> player.setMatchPoints(0));
    }

    /**
     * Passa al prossimo turno, il giocatore deve girare la ruota per determinare il bonus
     *
     * @param force Per forzare il cambio di giocatore
     * @param broadcaster Una funzione per scrivere il pacchetto al server
     */
    public void nextRound(boolean force, Consumer<NextRoundPacket> broadcaster) {
        if (!server) throw new IllegalArgumentException("Solo il server può eseguire questa funzione");
        if (players.isEmpty()) return;

        GamePlayer nextPlayer;

        // Scelta di un giocatore casuale. Se è il primo round, scegli il primo giocatore
        if (currentRound == null) nextPlayer = players.getFirst();
        else {
            // Se è stato forzato (ad esempio quando un giocatore sbaglia) oppure il bonus è negativo, cambia giocatore
            if (force || (currentRound.getBonus() != null && 
                    (currentRound.getBonus().equals(Round.Bonus.BANCAROTTA) ||
                    currentRound.getBonus().equals(Round.Bonus.SALTA)))) {
                int index = players.indexOf(currentRound.getPlayer());

                if ((index + 1) >= players.size()) index = 0;
                else index++;

                nextPlayer = players.get(index);
            } else {
                nextPlayer = currentRound.getPlayer();
            }
        }

        this.currentRound = new Round(nextPlayer);

        broadcaster.accept(new NextRoundPacket(currentRound));
    }

    /**
     * Prova a indovinare un carattere. Rimuove una vita se non presente e se l'utente non ha mai provato ad indovinarla
     *
     * @param ch Il carattere da indovinare
     * @return True se il tentativo è corretto e mai utilizzato
     */
    public boolean guessChar(char ch) {
        if (!server) throw new IllegalArgumentException("Solo il server può eseguire questa funzione");

        ch = Character.toLowerCase(ch);

        if (guesses.contains(ch)) return false;

        guesses.add(ch);

        int found = 0;
        for (int i = 0; i < solution.length(); i++) {
            if (solution.charAt(i) == ch) {
                currentGuess[i] = ch;
                found++;
            }
        }

        int bonus = currentRound.getBonus().getMultiplier() * found;
        currentRound.getPlayer().addMatchPoints(bonus);

        if (found < 1) lives--;

        return found > 0;
    }

    /**
     * Prova a indovinare l'intera frase. Rimuove una vita se errata
     *
     * @param string La frase da indovinare
     * @return Ritorna se è corretta o meno
     */
    public boolean guessString(String string) {
        if (!server) throw new IllegalArgumentException("Solo il server può eseguire questa funzione");

        if (solution.equalsIgnoreCase(string)) {
            int missingLetters = 0;

            for (Character guess : currentGuess) {
                if (guess == null) missingLetters++;
            }

            int bonus = currentRound.getBonus().getMultiplier() * missingLetters;
            currentRound.getPlayer().addMatchPoints(bonus);

            this.currentGuess = toCharArray(string.toCharArray());
            this.handleWin(currentRound.getPlayer());
            return true;
        }

        lives--;
        return false;
    }

    /**
     * @return True se l'intera frase è stata indovinata
     */
    public boolean isComplete() {
        return toCharString(currentGuess).equalsIgnoreCase(solution);
    }

    /**
     * Gestisce la vittoria di un giocatore. Assegna a questo i punti guadagnati
     *
     * @param player Il giocatore che ha vinto
     */
    public void handleWin(GamePlayer player) {
        if (!server) throw new IllegalArgumentException("Solo il server può eseguire questa funzione");

        player.addPoints(player.getMatchPoints());
        player.setMatchPoints(0);
    }

    /**
     * Ritorna una stringa contenete le vite e ciò che l'utente attualmente conosce della stringa.
     * Il formato è: VITE:CATEGORIA:------------ (es. 5:OGGETTO:--a-o)
     *
     * @return La stringa serializzata o "NULL" se il gioco non è in corso
     */
    public String serialize() {
        if (!started) return "NULL";

        StringBuilder serialized = new StringBuilder(lives + ":" + category + ":");

        for (Character ch : currentGuess) {
            if (ch == null) serialized.append("-");
            else serialized.append(ch);
        }

        return serialized.toString();
    }

    /**
     * Aggiorna il gioco corrente data la stringa serializzata usando l'apposita funzione
     *
     * @param serialized La stringa serializzata
     * @see Game#serialize
     */
    public void deserialize(String serialized) {
        if (serialized.equalsIgnoreCase("NULL")) {
            started = false;
            return;
        }

        String[] split = serialized.split(":");

        this.lives = Integer.parseInt(split[0]);
        this.category = split[1];
        this.currentGuess = toCharArray(split[2].toCharArray());
    }

    /**
     * Funzione di utilità per convertire un array di char(nativo) a un array di Character(oggetto).
     * Se il carattere è '-' verrà assegnato null a quella posizione
     *
     * @param chars L'array di char nativi
     * @return L'array di {@link Character}
     */
    public Character[] toCharArray(char[] chars) {
        Character[] characters = new Character[chars.length];

        for (int i = 0; i < chars.length; i++) {
            characters[i] = chars[i] == '-' ? null : chars[i];
        }

        return characters;
    }

    /**
     * Funzione di utilità per convertire un array di Character(oggetto) a una stringa.
     *
     * @param chars L'array di char
     * @return La stringa concatenata
     */
    public String toCharString(Character[] chars) {
        StringBuilder serialized = new StringBuilder();

        for (Character ch : chars) {
            if (ch == null) serialized.append("-");
            else serialized.append(ch);
        }

        return serialized.toString();
    }

    /**
     * @return la stringa dello stato corrente conosciuto dal client per il gioco.
     */
    public String toGuessesString() {
        if (currentGuess == null) return "";

        StringBuilder builder = new StringBuilder();

        for (Character guess : currentGuess) {
            builder.append(guess == null ? '-' : guess);
        }

        return builder.toString();
    }


    /**
     * Ritorna il giocatore con l'id specificato
     *
     * @param uuid L'id del giocatore
     * @return Il giocatore o null se non esiste
     */
    public GamePlayer getPlayerById(UUID uuid) {
        return players.stream().filter(p -> p.getUniqueId().equals(uuid)).findFirst().orElse(null);
    }

    public int getLives() {
        return lives;
    }

    public String getSolution() {
        return solution;
    }

    public String getCategory() {
        return category;
    }

    public Character[] getCurrentGuess() {
        return currentGuess;
    }

    public List<Character> getGuesses() {
        return guesses;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public Round getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(Round currentRound) {
        this.currentRound = currentRound;
    }

    public List<GamePlayer> getPlayers() {
        return players;
    }

    public static Game getInstance() {
        return instance;
    }
}
