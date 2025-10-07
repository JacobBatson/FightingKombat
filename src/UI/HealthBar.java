package UI;

import Engine.GraphicsHandler;
import java.awt.Color;

/**
 * Simple HealthBar renderer.
 * Draws a filled rectangle showing hp/maxHp. Supports RTL for right-aligned shrinking.
 */
public class HealthBar {

    private Color fg;
    private Color bg;

    public HealthBar() {
        this(Color.GREEN, new Color(0, 0, 0, 160));
    }

    public HealthBar(Color fg, Color bg) {
        this.fg = fg;
        this.bg = bg;
    }

  
    public void draw(GraphicsHandler g, int x, int y, int w, int h, int hp, int maxHp, boolean rtl) {
        if (g == null) return;
        if (maxHp <= 0) maxHp = 1;
        int clamped = Math.max(0, Math.min(hp, maxHp));
        float frac = (float) clamped / (float) maxHp;

        // background
        g.drawFilledRectangle(x, y, w, h, bg);
        g.drawRectangle(x, y, w, h, Color.BLACK);

        int fillW = Math.round(frac * w);
        if (fillW > 0) {
            if (rtl) {
                int fx = x + (w - fillW);
                g.drawFilledRectangle(fx, y, fillW, h, fg);
            } else {
                g.drawFilledRectangle(x, y, fillW, h, fg);
            }
        }
    }
}
