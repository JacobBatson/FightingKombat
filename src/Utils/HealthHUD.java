package Utils;

import Engine.GraphicsHandler;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Path2D;

/**
 * Utility renderer for drawing heart-based health indicators on the HUD.
 */
public class HealthHUD {

    private final int heartSize;
    private final int heartSpacing;
    private final Color filledColor = new Color(220, 20, 60);
    private final Color emptyColor = new Color(100, 100, 100);
    private final Color outlineColor = new Color(35, 35, 35);

    public HealthHUD() {
        this(26, 6);
    }

    public HealthHUD(int heartSize, int heartSpacing) {
        this.heartSize = heartSize;
        this.heartSpacing = heartSpacing;
    }

    public int getHeartSize() {
        return heartSize;
    }

    public int getHeartSpacing() {
        return heartSpacing;
    }

    public void drawHearts(GraphicsHandler graphicsHandler, int currentHearts, int maxHearts, int startX, int startY, boolean leftToRight) {
        if (maxHearts <= 0) {
            return;
        }

        Graphics2D g2d = graphicsHandler.getGraphics();
        Object oldAA = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < maxHearts; i++) {
            int offset = i * (heartSize + heartSpacing);
            int heartX = leftToRight ? startX + offset : startX - offset;
            drawHeart(g2d, heartX, startY, emptyColor);
        }

        for (int i = 0; i < currentHearts; i++) {
            int offset = i * (heartSize + heartSpacing);
            int heartX = leftToRight ? startX + offset : startX - offset;
            drawHeart(g2d, heartX, startY, filledColor);
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
    }

    private void drawHeart(Graphics2D g2d, int x, int y, Color fillColor) {
        Path2D.Double heart = new Path2D.Double();
        double size = heartSize;
        heart.moveTo(x + size / 2.0, y + size);
        heart.curveTo(x + size * 1.1, y + size * 0.75, x + size, y + size * 0.25, x + size * 0.5, y + size * 0.35);
        heart.curveTo(x, y + size * 0.25, x - size * 0.1, y + size * 0.75, x + size / 2.0, y + size);

        Color previousColor = g2d.getColor();
        Stroke previousStroke = g2d.getStroke();

        g2d.setColor(fillColor);
        g2d.fill(heart);

        g2d.setColor(outlineColor);
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(heart);

        g2d.setColor(previousColor);
        g2d.setStroke(previousStroke);
    }
}
