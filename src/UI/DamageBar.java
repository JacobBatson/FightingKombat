package UI;

import Engine.GraphicsHandler;
import java.awt.Color;

/**
 * DamageBar renderer for tracking damage dealt by players.
 * Draws a filled rectangle showing damageDealt/maxDamage.
 */
public class DamageBar {
    private Color fg;
    private Color bg;

    public DamageBar() {
        this(new Color(255, 165, 0), new Color(0, 0, 0, 160)); // Orange foreground
    }

    public DamageBar(Color fg, Color bg) {
        this.fg = fg;
        this.bg = bg;
    }

    public void draw(GraphicsHandler g, int x, int y, int w, int h, int damageDealt, int maxDamage, boolean rtl) {
        if (g == null)
            return;
        if (maxDamage <= 0)
            maxDamage = 1;
        int clamped = Math.max(0, Math.min(damageDealt, maxDamage));
        float frac = (float) clamped / (float) maxDamage;

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
