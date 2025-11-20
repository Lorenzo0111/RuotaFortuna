package game.client.ui.panels;

import game.GamePlayer;
import game.client.Client;
import game.client.ui.ClientGUI;
import game.packets.GamePacket;
import game.packets.client.TryPacket;
import game.packets.common.SpinPacket;
import game.packets.server.ErrorPacket;
import game.packets.server.LosePacket;
import game.packets.server.NextRoundPacket;
import game.packets.server.WinPacket;
import game.utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

import static game.utils.UIColors.*;
import static game.utils.UIUtils.createStyledButton;
import static game.utils.UIUtils.showCustomDialog;

/**
 * Panel personalizzato per il gioco
 */
public class GamePanel extends JFrame {
    private final ClientGUI gui;
    private final Client client;
    private JPanel stickmanPanel;
    private WheelPanel wheelPanel;
    private JLabel roundLabel;
    private PlayerListPanel listPanel;
    private JLabel categoryLabel;
    private JLabel sentenceLabel;
    private JTextField inputField;
    private JButton submitButton;

    public GamePanel(ClientGUI gui) {
        this.gui = gui;
        this.client = gui.getClient();

        setTitle("Gioco dell'Impiccato");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        UIUtils.setIcon(this);

        this.initializeGameUI();
    }

    /**
     * Inizializza l'interfaccia di gioco
     */
    private void initializeGameUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Card per lo stickman
        JPanel stickmanCard = new JPanel(new BorderLayout());
        stickmanCard.setBackground(CARD_COLOR);
        stickmanCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        stickmanPanel = new StickmanPanel(client);
        stickmanPanel.setPreferredSize(new Dimension(320, 420));
        stickmanPanel.setBackground(CARD_COLOR);

        wheelPanel = new WheelPanel();
        wheelPanel.setOpaque(false);
        wheelPanel.setSpinCallback(() -> {
            try {
                client.write(new SpinPacket(null));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        wheelPanel.setSpinCompleteCallback(() -> {
            // Re-abilita l'input solo se è il turno del giocatore e ha già girato la ruota
            if (client.getGame().getCurrentRound() != null) {
                boolean isPlayerTurn = client.getGame().getCurrentRound().getPlayer().getUniqueId().equals(client.getPlayerId());
                boolean hasSpun = client.getGame().getCurrentRound().getBonus() != null;
                boolean canInput = isPlayerTurn && hasSpun;

                inputField.setEnabled(canInput);
                submitButton.setEnabled(canInput);

                if (canInput) inputField.requestFocusInWindow();

                if (isPlayerTurn) {
                    if (hasSpun) {
                        inputField.setText("");
                    } else {
                        inputField.setText("Gira la ruota!");
                    }
                } else {
                    inputField.setText("Non è il tuo turno");
                }
            }
        });

        JPanel stickmanContainer = new JPanel();
        stickmanContainer.setOpaque(false);
        stickmanContainer.setLayout(new BoxLayout(stickmanContainer, BoxLayout.Y_AXIS));
        stickmanPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        stickmanContainer.add(stickmanPanel);
        stickmanContainer.add(Box.createVerticalStrut(20));

        wheelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        stickmanContainer.add(wheelPanel);

        stickmanCard.add(stickmanContainer, BorderLayout.CENTER);

        // Panel centrale con info gioco
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setBackground(BACKGROUND_COLOR);

        // Card per il turno
        JPanel roundCard = new JPanel(new BorderLayout());
        roundCard.setBackground(CARD_COLOR);
        roundCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(20, 30, 20, 30)
        ));

        roundLabel = new JLabel("Caricamento..", SwingConstants.CENTER);
        roundLabel.setFont(new Font("Arial", Font.BOLD, 24));
        roundLabel.setForeground(PRIMARY_COLOR);
        roundCard.add(roundLabel, BorderLayout.CENTER);

        centerPanel.add(roundCard, BorderLayout.NORTH);

        listPanel = new PlayerListPanel(client, true);

        sentenceLabel = new JLabel("", SwingConstants.CENTER);
        sentenceLabel.setFont(new Font("Courier New", Font.BOLD, 32));
        sentenceLabel.setForeground(TEXT_COLOR);

        categoryLabel = new JLabel("", SwingConstants.CENTER);
        categoryLabel.setFont(new Font("Courier New", Font.BOLD, 20));
        categoryLabel.setForeground(TEXT_COLOR);

        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(25, 30, 25, 30)
        ));

        panel.add(sentenceLabel);
        panel.add(categoryLabel);
        panel.add(listPanel);

        centerPanel.add(panel, BorderLayout.CENTER);

        // Card per input
        JPanel inputCard = new JPanel(new BorderLayout(0, 15));
        inputCard.setBackground(CARD_COLOR);
        inputCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(25, 30, 25, 30)
        ));

        JLabel inputLabel = new JLabel("Inserisci una lettera o la frase:");
        inputLabel.setFont(new Font("Arial", Font.BOLD, 14));
        inputLabel.setForeground(TEXT_COLOR);
        inputCard.add(inputLabel, BorderLayout.NORTH);

        JPanel inputFieldPanel = new JPanel(new BorderLayout(15, 0));
        inputFieldPanel.setBackground(CARD_COLOR);

        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 16));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                new EmptyBorder(12, 15, 12, 15)
        ));
        inputField.setEnabled(false);
        inputField.setText("Non è il tuo turno");
        inputFieldPanel.add(inputField, BorderLayout.CENTER);

        submitButton = createStyledButton("Invia", PRIMARY_COLOR);
        submitButton.setPreferredSize(new Dimension(120, 48));
        submitButton.setEnabled(false);
        inputFieldPanel.add(submitButton, BorderLayout.EAST);

        inputCard.add(inputFieldPanel, BorderLayout.CENTER);
        centerPanel.add(inputCard, BorderLayout.SOUTH);

        // Aggiungi tutto al panel principale
        mainPanel.add(stickmanCard, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);

        submitButton.addActionListener(e -> sendGuess());
        inputField.addActionListener(e -> sendGuess());
    }

    /**
     * Invia la lettera/frase al server
     */
    private void sendGuess() {
        String input = inputField.getText().trim();

        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Inserisci una lettera o una frase!",
                    "Errore",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            client.write(new TryPacket(input));
            inputField.setText("");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Errore durante l'invio: " + e.getMessage(),
                    "Errore",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Aggiorna l'interfaccia in base al risultato
     */
    public void updateUI(GamePacket packet) {
        GamePlayer self = client.getPlayerId() != null ? client.getGame().getPlayerById(client.getPlayerId()) : null;
        if (self != null) this.setTitle("Gioco dell'Impiccato - " + self.getUsername());

        if (packet instanceof NextRoundPacket nextRoundPacket) {
            boolean playerTurn = nextRoundPacket.getRound().getPlayer().getUniqueId().equals(client.getPlayerId());

            // Se il bonus è null, il giocatore deve girare la ruota
            if (nextRoundPacket.getRound().getBonus() == null) {
                wheelPanel.setInteractive(playerTurn);
                wheelPanel.spinTo(null);
                inputField.setEnabled(false);
                submitButton.setEnabled(false);
                if (playerTurn) inputField.setText("Gira la ruota!");
                else
                    inputField.setText("In attesa che " + nextRoundPacket.getRound().getPlayer().getUsername() + " giri la ruota");
            } else {
                // Se il bonus è stato determinato, mostra l'animazione
                wheelPanel.setInteractive(false);
                inputField.setEnabled(false);
                submitButton.setEnabled(false);
                inputField.setText("La ruota sta girando...");
                wheelPanel.spinTo(nextRoundPacket.getRound().getBonus());
            }
        }

        listPanel.update();

        int lives = client.getGame().getLives();

        if (client.getGame().getCurrentRound() != null)
            roundLabel.setText("Turno di " + client.getGame().getCurrentRound().getPlayer().getUsername());

        sentenceLabel.setText(client.getGame().toGuessesString());
        categoryLabel.setText("Argomento: " + client.getGame().getCategory());

        stickmanPanel.repaint();
        wheelPanel.repaint();

        if (packet instanceof WinPacket p) {
            if (p.getVincitore().equals(client.getPlayerId()))
                showCustomDialog(
                        this,
                        "HAI VINTO!",
                        "Complimenti! La frase era:\n" + p.getSoluzione(),
                        SUCCESS_COLOR
                );
            else showCustomDialog(
                    this,
                    client.getGame().getPlayerById(p.getVincitore()).getUsername().toUpperCase() + " HA VINTO!",
                    "La frase era:\n" + p.getSoluzione(),
                    SUCCESS_COLOR
            );
            gui.showWaitingPanel();
        }

        if (packet instanceof LosePacket p) {
            showCustomDialog(
                    this,
                    "Sconfitta",
                    "Sono terminate le vite!\nLa frase era:\n" + p.getSoluzione(),
                    ERROR_COLOR
            );
            gui.showWaitingPanel();
        }

        if (packet instanceof ErrorPacket) {
            showCustomDialog(
                    this,
                    "Tentativo Errato",
                    "SBAGLIATO! Ora hai " + lives + " " + (lives == 1 ? "vita" : "vite"),
                    WARNING_COLOR
            );
        }
    }
}
