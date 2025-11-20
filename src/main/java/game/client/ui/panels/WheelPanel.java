package game.client.ui.panels;

import game.Round;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import static game.utils.UIColors.*;

/**
 * Panel personalizzato per disegnare la ruota della fortuna
 */
public class WheelPanel extends JPanel {
    private final JLabel bonusLabel;
    private final Round.Bonus[] bonuses = Round.Bonus.values();
    private final Map<Round.Bonus, Color> colors = new HashMap<>();

    private double rotation = 0;
    private Timer timer;
    private boolean interactive = false;
    private Runnable spinCallback;

    private double lastAngle = 0;
    private long lastTime = 0;
    private double velocity = 0;
    private boolean dragging = false;
    private boolean waitingForResult = false;
    private Runnable spinCompleteCallback;

    public WheelPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(16, 0, 0, 0));
        setPreferredSize(new Dimension(280, 300));

        colors.put(Round.Bonus.M500, PRIMARY_COLOR);
        colors.put(Round.Bonus.M200, SECONDARY_COLOR);
        colors.put(Round.Bonus.M100, SUCCESS_COLOR);
        colors.put(Round.Bonus.SALTA, WARNING_COLOR);
        colors.put(Round.Bonus.BANCAROTTA, ERROR_COLOR);

        bonusLabel = new JLabel("Ruota della Fortuna", SwingConstants.CENTER);
        bonusLabel.setForeground(TEXT_SECONDARY);
        bonusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(bonusLabel, BorderLayout.SOUTH);

        setupMouseListeners();
    }

    /**
     * Avvia il listener del mouse per ascoltare i movimenti della ruota
     */
    private void setupMouseListeners() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!interactive) return;
                dragging = true;
                lastAngle = getAngleFromMouse(e.getX(), e.getY());
                lastTime = System.currentTimeMillis();
                velocity = 0;
                if (timer != null) timer.stop();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!interactive || !dragging) return;

                double newAngle = getAngleFromMouse(e.getX(), e.getY());
                long newTime = System.currentTimeMillis();
                double deltaAngle = newAngle - lastAngle;
                long deltaTime = newTime - lastTime;

                if (deltaAngle > 180) deltaAngle -= 360;
                if (deltaAngle < -180) deltaAngle += 360;

                rotation += deltaAngle;

                if (deltaTime > 0) velocity = deltaAngle / deltaTime * 16;

                lastAngle = newAngle;
                lastTime = newTime;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!interactive || !dragging) return;
                dragging = false;

                if (Math.abs(velocity) < 0.5) velocity = 3.0;

                startDeceleration();
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    /**
     * Calcola l'angolo tra il centro del componente e le coordinate del mouse
     *
     * @param mouseX La coordinata x del mouse
     * @param mouseY La coordinata y del mouse
     * @return L'angolo in gradi
     */
    private double getAngleFromMouse(int mouseX, int mouseY) {
        int centerX = getWidth() / 2;
        int centerY = (getHeight() - 50) / 2;
        double angle = Math.toDegrees(Math.atan2(centerY - mouseY, mouseX - centerX));
        return angle < 0 ? angle + 360 : angle;
    }

    /**
     * Avvia la decelerazione della ruota
     */
    private void startDeceleration() {
        bonusLabel.setText("In attesa del risultato...");
        bonusLabel.setForeground(TEXT_SECONDARY);
        waitingForResult = true;

        // Invia il callback
        if (spinCallback != null) spinCallback.run();

        // La ruota continua a girare a velocità costante moderata mentre aspetta il risultato
        velocity = Math.signum(velocity) * Math.max(3.0, Math.abs(velocity) * 0.3);

        timer = new Timer(16, e -> {
            rotation += velocity;
            repaint();
        });
        timer.start();
    }

    /**
     * Modifica l'interattività della ruota
     * @param interactive True quando è possibile girare la ruota con il mouse
     */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
        if (interactive) {
            waitingForResult = false;
            bonusLabel.setText("Trascina per girare la ruota!");
            bonusLabel.setForeground(PRIMARY_COLOR);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Imposta il callback da eseguire quando la ruota viene girata
     * @param callback La funzione da eseguire
     */
    public void setSpinCallback(Runnable callback) {
        this.spinCallback = callback;
    }

    /**
     * Imposta il callback da eseguire quando la ruota termina di girare
     * @param callback La funzione da eseguire
     */
    public void setSpinCompleteCallback(Runnable callback) {
        this.spinCompleteCallback = callback;
    }

    /**
     * Gira la ruota verso un bonus specifico
     * @param bonus Il bonus da girare
     */
    public void spinTo(Round.Bonus bonus) {
        if (bonus == null) {
            if (!waitingForResult) {
                bonusLabel.setText("In attesa...");
                bonusLabel.setForeground(TEXT_SECONDARY);
            }
            return;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> spinTo(bonus));
            return;
        }

        setInteractive(false);
        waitingForResult = false;

        if (timer != null) timer.stop();

        bonusLabel.setText("Ruota in corso...");
        bonusLabel.setForeground(TEXT_SECONDARY);

        double targetAngle = getAngleForBonus(bonus);
        final double startRot = rotation;

        // Fai come minimo 3 giri completi per allungare l'animazione
        double currentNormalized = rotation % 360;
        double rotationsNeeded = 720 + 360;
        double calculatedTarget = rotation - currentNormalized + rotationsNeeded + (90 - targetAngle);

        while (calculatedTarget <= rotation) {
            calculatedTarget += 360;
        }

        final double targetRot = calculatedTarget;
        final long startTime = System.currentTimeMillis();
        final long animationDuration = 3500;

        timer = new Timer(16, e -> {
            double progress = Math.min(1.0, (System.currentTimeMillis() - startTime) / (double) animationDuration);
            rotation = startRot + (targetRot - startRot) * (1 - Math.pow(1 - progress, 3));
            repaint();

            if (progress >= 1.0) {
                timer.stop();
                rotation = targetRot;
                bonusLabel.setForeground(TEXT_COLOR);
                bonusLabel.setText(bonus.getLongText());

                if (spinCompleteCallback != null) spinCompleteCallback.run();
            }
        });
        timer.start();
    }

    /**
     * Ritorna l'angolo della ruota in cui è presente il bonus richiesto
     * @param bonus Il bonus da selezionare
     * @return L'angolo in gradi
     */
    private double getAngleForBonus(Round.Bonus bonus) {
        double total = Round.Bonus.getTotal();
        double angle = 0;
        for (Round.Bonus b : bonuses) {
            double extent = (b.getWeight() / total) * 360;
            if (b == bonus) return angle + extent / 2;
            angle += extent;
        }
        return 0;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.max(Math.min(getWidth(), getHeight() - 50) - 20, 100);
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size - 50) / 2;

        // Disegna segmenti
        double total = Round.Bonus.getTotal();
        double currentAngle = rotation % 360;
        double startAngle = 0;

        for (Round.Bonus bonus : bonuses) {
            double extent = (bonus.getWeight() / total) * 360;
            int start = (int) ((startAngle + currentAngle) % 360);

            g2.setColor(colors.get(bonus));
            g2.fillArc(x, y, size, size, start, (int) extent);
            g2.setColor(BORDER_COLOR);
            g2.drawArc(x, y, size, size, start, (int) extent);

            // Label
            double midAngle = Math.toRadians(start + extent / 2);
            int labelX = x + size / 2 + (int) (Math.cos(midAngle) * size * 0.3);
            int labelY = y + size / 2 - (int) (Math.sin(midAngle) * size * 0.3);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(bonus.getShortText(), labelX - fm.stringWidth(bonus.getShortText()) / 2, labelY + fm.getAscent() / 3);

            startAngle += extent;
        }

        // Bordo ruota
        g2.setColor(new Color(0, 0, 0, 25));
        g2.setStroke(new BasicStroke(3f));
        g2.drawOval(x, y, size, size);

        // Puntatore
        int cx = getWidth() / 2;
        int[] px = {cx, cx - 13, cx + 13};
        int[] py = {y - 6 + 24, y - 6, y - 6};
        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillPolygon(px, py, 3);
        g2.setColor(PRIMARY_COLOR);
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(px, py, 3);

        g2.dispose();
    }
}


