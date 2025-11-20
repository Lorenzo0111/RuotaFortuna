package game.client.ui.panels;

import game.GamePlayer;
import game.client.Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static game.utils.UIColors.*;

/**
 * Interfaccia per la lista dei giocatori connessi alla partita
 */
public class PlayerListPanel extends JPanel {
    private final Client client;
    private final boolean inGame;
    private JPanel playersListPanel;

    /**
     * Crea il pannello per la lista dei giocatori
     * @param client L'istanza del client
     * @param inGame True se si desidera mostrare anche il punteggio del round e l'ultimo tentativo
     */
    public PlayerListPanel(Client client, boolean inGame) {
        super(new BorderLayout(0, 20));

        this.client = client;
        this.inGame = inGame;
        this.init();
    }

    /**
     * Crea il pannello
     */
    private void init() {
        this.setBackground(BACKGROUND_COLOR);

        // Card per la lista giocatori
        JPanel playersCard = new JPanel(new BorderLayout(0, 15));
        playersCard.setBackground(CARD_COLOR);
        playersCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(25, 25, 25, 25)
        ));

        // Titolo lista giocatori
        JPanel playersHeaderPanel = new JPanel(new BorderLayout());
        playersHeaderPanel.setBackground(CARD_COLOR);

        JLabel playersLabel = new JLabel("Giocatori connessi");
        playersLabel.setFont(new Font("Arial", Font.BOLD, 18));
        playersLabel.setForeground(TEXT_COLOR);
        playersHeaderPanel.add(playersLabel, BorderLayout.WEST);

        playersCard.add(playersHeaderPanel, BorderLayout.NORTH);

        // Scroll panel per la lista
        playersListPanel = new JPanel();
        playersListPanel.setLayout(new BoxLayout(playersListPanel, BoxLayout.Y_AXIS));
        playersListPanel.setBackground(CARD_COLOR);

        JScrollPane scrollPane = new JScrollPane(playersListPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        playersCard.add(scrollPane, BorderLayout.CENTER);

        this.add(playersCard, BorderLayout.CENTER);
    }

    /**
     * Aggiorna la lista dei giocatori
     */
    public void update() {
        playersListPanel.removeAll();

        List<GamePlayer> players = new ArrayList<>(client.getGame().getPlayers());
        UUID currentTurn = client.getGame().getCurrentRound() != null ?
                client.getGame().getCurrentRound().getPlayer().getUniqueId() :
                null;

        players.sort((player1, player2) -> {
            if (player1.getUniqueId().equals(currentTurn)) {
                return -1;
            } else if (player2.getUniqueId().equals(currentTurn)) {
                return 1;
            } else {
                return player1.getUsername().compareToIgnoreCase(player2.getUsername());
            }
        });

        if (players.isEmpty()) {
            JLabel emptyLabel = new JLabel("Nessun giocatore connesso");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            emptyLabel.setForeground(TEXT_LIGHT);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            playersListPanel.add(Box.createVerticalStrut(20));
            playersListPanel.add(emptyLabel);
        } else {
            for (GamePlayer player : players) {
                playersListPanel.add(createPlayerCard(player));
                playersListPanel.add(Box.createVerticalStrut(10));
            }
        }

        playersListPanel.revalidate();
        playersListPanel.repaint();
    }

    /**
     * Crea una card per visualizzare un giocatore
     */
    private JPanel createPlayerCard(GamePlayer player) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(BACKGROUND_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, inGame ? 100 : 75));

        // Avatar (iniziale del nome)
        String initial = player.getUsername().substring(0, 1).toUpperCase();
        JLabel avatarLabel = new JLabel(initial);
        avatarLabel.setFont(new Font("Arial", Font.BOLD, 20));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setBackground(PRIMARY_COLOR);
        avatarLabel.setOpaque(true);
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setPreferredSize(new Dimension(45, 45));
        card.add(avatarLabel, BorderLayout.WEST);

        // Info giocatore
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(BACKGROUND_COLOR);

        JLabel nameLabel = new JLabel(player.getUsername());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(TEXT_COLOR);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(nameLabel);

        infoPanel.add(Box.createVerticalStrut(5));

        JLabel pointsLabel = new JLabel("Punti: " + player.getPoints());
        pointsLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        pointsLabel.setForeground(TEXT_SECONDARY);
        pointsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(pointsLabel);

        if (inGame) {
            JLabel matchPointsLabel = new JLabel("Punti Round: " + player.getMatchPoints());
            matchPointsLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            matchPointsLabel.setForeground(TEXT_SECONDARY);
            matchPointsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoPanel.add(matchPointsLabel);

            if (player.getLastGuess() != null && !player.getLastGuess().isEmpty()) {
                JLabel lastGuessLabel = new JLabel("Ultimo Tentativo: " + player.getLastGuess());
                lastGuessLabel.setFont(new Font("Arial", Font.PLAIN, 13));
                lastGuessLabel.setForeground(TEXT_SECONDARY);
                lastGuessLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                infoPanel.add(lastGuessLabel);
            }
        }

        card.add(infoPanel, BorderLayout.CENTER);

        return card;
    }

}
