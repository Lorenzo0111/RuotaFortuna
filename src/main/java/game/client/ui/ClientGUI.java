package game.client.ui;

import game.client.Client;
import game.client.ui.panels.GamePanel;
import game.client.ui.panels.WaitingPanel;
import game.packets.common.RestartPacket;
import game.packets.server.GameErrorPacket;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Interfaccia grafica per il gioco dell'impiccato con ruota della fortuna
 */
public class ClientGUI {
    private Client client;
    private WaitingPanel waitingPanel;
    private GamePanel gamePanel;

    /**
     * Costruttore della GUI Client. Alla creazione mostra il dialog di selezione del server
     */
    public ClientGUI() {
        showConnectionDialog();
    }

    /**
     * Mostra il dialog per connettersi al server
     */
    private void showConnectionDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel ipLabel = new JLabel("Indirizzo Server:");
        JTextField ipField = new JTextField("localhost");

        JLabel portLabel = new JLabel("Porta:");
        JTextField portField = new JTextField("6789");

        panel.add(ipLabel);
        panel.add(ipField);
        panel.add(portLabel);
        panel.add(portField);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Connessione al Server",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String ip = ipField.getText().trim();
            int port;

            try {
                port = Integer.parseInt(portField.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Porta non valida!",
                        "Errore",
                        JOptionPane.ERROR_MESSAGE
                );
                showConnectionDialog();
                return;
            }

            connectToServer(ip, port);
        } else {
            System.exit(0);
        }
    }

    /**
     * Connette al server e inizializza l'interfaccia
     *
     * @param ip   IP del server
     * @param port Porta del server
     */
    private void connectToServer(String ip, int port) {
        client = new Client(ip, port);
        client.setUpdateHandler(result -> {
            if (result instanceof GameErrorPacket errorPacket) {
                JOptionPane.showMessageDialog(
                        null,
                        errorPacket.getType().getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE
                );

                if (errorPacket.getType().shouldDisconnect()) {
                    this.showConnectionDialog();

                    gamePanel.setVisible(false);
                    waitingPanel.setVisible(false);

                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (result instanceof RestartPacket) {
                gamePanel.updateUI(result);

                gamePanel.setVisible(true);
                waitingPanel.setVisible(false);
                return;
            }

            // Aggiorna l'interfaccia sul thread principale
            if (gamePanel != null && gamePanel.isVisible())
                SwingUtilities.invokeLater(() -> gamePanel.updateUI(result));

            if (waitingPanel != null && waitingPanel.isVisible())
                SwingUtilities.invokeLater(() -> waitingPanel.updateUI(result));
        });

        // Se la connessione non è riuscita, mostra un errore
        if (!client.connect()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Connessione al server fallita!",
                    "Errore",
                    JOptionPane.ERROR_MESSAGE
            );
            showConnectionDialog();
            return;
        }

        waitingPanel = new WaitingPanel(this);
        gamePanel = new GamePanel(this);

        waitingPanel.setVisible(true);
    }

    public void showWaitingPanel() {
        waitingPanel.setVisible(true);
        gamePanel.setVisible(false);
    }

    /**
     * Chiude il gioco
     */
    public void closeGame() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public Client getClient() {
        return client;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            new ClientGUI();
        });
    }
}