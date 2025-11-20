package game.client.ui.panels;

import game.Game;
import game.client.Client;

import javax.swing.*;
import java.awt.*;

/**
 * Panel personalizzato per disegnare lo stickman
 */
public class StickmanPanel extends JPanel {
    private final Client client;

    public StickmanPanel(Client client) {
        this.client = client;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(3));

        int width = getWidth();
        int height = getHeight();

        int missingPieces = Game.MAX_LIVES - client.getGame().getLives();

        // Palo verticale
        g2d.setColor(new Color(139, 69, 19));
        g2d.fillRect(width / 2 - 10, 50, 20, height - 70);

        // Palo orizzontale
        g2d.fillRect(width / 2 - 10, 50, width / 3, 20);

        // Corda
        g2d.setColor(Color.BLACK);
        g2d.drawLine(width / 2 + width / 4, 70, width / 2 + width / 4, 120);

        if (missingPieces == 0) return;

        // Disegna parti dello stickman in base alle vite perse
        int x = width / 2 + width / 4;
        int y = 120;

        // Testa
        if (missingPieces >= 1) {
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x - 25, y, 50, 50);
        }

        // Corpo
        if (missingPieces >= 2) {
            g2d.drawLine(x, y + 50, x, y + 120);
        }

        // Braccio sinistro
        if (missingPieces >= 3) {
            g2d.drawLine(x, y + 70, x - 35, y + 100);
        }

        // Braccio destro
        if (missingPieces >= 4) {
            g2d.drawLine(x, y + 70, x + 35, y + 100);
        }

        // Gamba sinistra
        if (missingPieces >= 5) {
            g2d.drawLine(x, y + 120, x - 30, y + 180);
        }

        // Gamba destra
        if (missingPieces >= 6) {
            g2d.drawLine(x, y + 120, x + 30, y + 180);
        }
    }
}