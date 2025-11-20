package game.client.ui.panels;

import game.client.Client;
import game.client.ui.ClientGUI;
import game.packets.GamePacket;
import game.packets.client.AuthPacket;
import game.packets.common.RestartPacket;
import game.packets.server.GameErrorPacket;
import game.utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

import static game.utils.UIColors.*;
import static game.utils.UIUtils.createStyledButton;

/**
 * Panel personalizzato per la sala d'attesa della partita
 */
public class WaitingPanel extends JFrame {
    private final Client client;
    private JPanel mainPanel;
    private JPanel authPanel;
    private PlayerListPanel playersListPanel;
    private JTextField usernameField;
    private JButton startGameButton;
    private boolean authenticated = false;

    public WaitingPanel(ClientGUI gui) {
        this.client = gui.getClient();

        setTitle("Gioco dell'Impiccato - Lobby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        UIUtils.setIcon(this);

        initializeUI();
    }

    /**
     * Inizializza l'interfaccia principale
     */
    private void initializeUI() {
        mainPanel = new JPanel(new CardLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Crea i due pannelli principali
        createAuthPanel();
        createLobbyPanel();

        mainPanel.add(authPanel, "AUTH");
        mainPanel.add(playersListPanel, "LOBBY");

        add(mainPanel);

        // Mostra il pannello di autenticazione
        showAuthPanel();
    }

    /**
     * Crea il pannello di autenticazione
     */
    private void createAuthPanel() {
        authPanel = new JPanel();
        authPanel.setLayout(new GridBagLayout());
        authPanel.setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 40, 10, 40);

        // Card container
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(40, 40, 40, 40)
        ));

        // Titolo
        JLabel titleLabel = new JLabel("Benvenuto!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);

        card.add(Box.createVerticalStrut(10));

        // Sottotitolo
        JLabel subtitleLabel = new JLabel("Inserisci il tuo nome per entrare nella lobby");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitleLabel);

        card.add(Box.createVerticalStrut(40));

        // Label nome utente
        JLabel usernameLabel = new JLabel("Nome utente");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        usernameLabel.setForeground(TEXT_COLOR);
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(usernameLabel);

        card.add(Box.createVerticalStrut(10));

        // Campo di testo
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                new EmptyBorder(10, 15, 10, 15)
        ));
        card.add(usernameField);

        card.add(Box.createVerticalStrut(25));

        // Bottone entra
        JButton enterButton = createStyledButton("Entra nella Lobby", PRIMARY_COLOR);
        enterButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterButton.setPreferredSize(new Dimension(200, 50));
        enterButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        enterButton.addActionListener(e -> authenticate());
        card.add(enterButton);

        // Aggiungi listener per Enter
        usernameField.addActionListener(e -> authenticate());

        authPanel.add(card, gbc);
    }

    /**
     * Crea il pannello della lobby
     */
    private void createLobbyPanel() {
        playersListPanel = new PlayerListPanel(client, false);

        // Bottone per iniziare la partita
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BACKGROUND_COLOR);

        startGameButton = createStyledButton("Inizia Partita", SECONDARY_COLOR);
        startGameButton.setPreferredSize(new Dimension(200, 50));
        startGameButton.addActionListener(a -> this.startGame());
        startGameButton.setEnabled(client.getGame().getPlayers().size() >= 2);
        bottomPanel.add(startGameButton, BorderLayout.CENTER);

        playersListPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Autentica l'utente con il server
     */
    private void authenticate() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Inserisci un nome utente valido!",
                    "Errore",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            client.write(new AuthPacket(username));
            authenticated = true;
            showLobbyPanel();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Errore durante l'autenticazione: " + e.getMessage(),
                    "Errore",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Invia la richiesta di inizio partita al server
     */
    private void startGame() {
        try {
            client.write(new RestartPacket());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Errore durante l'avvio della partita: " + e.getMessage(),
                    "Errore",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Mostra il pannello di autenticazione
     */
    private void showAuthPanel() {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "AUTH");
    }

    /**
     * Mostra il pannello della lobby
     */
    private void showLobbyPanel() {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "LOBBY");
        playersListPanel.update();
    }

    /**
     * Aggiorna l'interfaccia in base al pacchetto ricevuto
     */
    public void updateUI(GamePacket packet) {
        if (packet instanceof GameErrorPacket errorPacket &&
                (errorPacket.getType().equals(GameErrorPacket.ErrorType.INVALID_USERNAME) ||
                        errorPacket.getType().equals(GameErrorPacket.ErrorType.USED_USERNAME))) {
            authenticated = false;
            showAuthPanel();
        }

        if (authenticated) SwingUtilities.invokeLater(() -> {
            playersListPanel.update();
            if (startGameButton != null) startGameButton.setEnabled(client.getGame().getPlayers().size() >= 2);
        });
    }

}
