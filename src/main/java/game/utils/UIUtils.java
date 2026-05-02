package game.utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

import static game.utils.UIColors.CARD_COLOR;
import static game.utils.UIColors.TEXT_COLOR;

/**
 * Funzioni utility per l'interfaccia utente
 */
public class UIUtils {

    /**
     * Crea un bottone
     *
     * @param text    Il testo del bottone
     * @param bgColor Il colore di sfondo
     * @return Il bottone stilizzato
     */
    public static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    /**
     * Mostra un dialog personalizzato
     *
     * @param parent  Il componente padre
     * @param title   Il titolo del dialog
     * @param message Il messaggio da mostrare
     * @param color   Il colore del titolo
     */
    public static void showCustomDialog(Component parent, String title, String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(CARD_COLOR);
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
            titleLabel.setForeground(color);
            panel.add(titleLabel, BorderLayout.NORTH);

            JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" +
                    message.replace("\n", "<br>") + "</div></html>", SwingConstants.CENTER);
            messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            messageLabel.setForeground(TEXT_COLOR);
            panel.add(messageLabel, BorderLayout.CENTER);

            JOptionPane.showMessageDialog(
                    parent,
                    panel,
                    title,
                    JOptionPane.PLAIN_MESSAGE
            );
        });
    }

    /**
     * Imposta l'icona al frame e all'app per MacOS
     *
     * @param frame Il frame da aggiornare
     */
    public static void setIcon(JFrame frame) {
        try (InputStream is = UIUtils.class.getResourceAsStream("/logo.png")) {
            if (is != null) {
                Image icon = Toolkit.getDefaultToolkit().createImage(is.readAllBytes());
                frame.setIconImage(icon);

                Taskbar taskbar = Taskbar.getTaskbar();
                taskbar.setIconImage(icon);
            } else {
                System.err.println("Impossibile trovare logo.png nelle risorse!");
            }
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
    }
}

