package UI;

import Engine.GraphicsHandler;
import Engine.ImageLoader;

import java.awt.Color;
import java.awt.image.BufferedImage;


public class HeartsHUD {
    public enum Anchor { LEFT, RIGHT }

    private final Anchor anchor;
    private final int marginX;
    private final int posY;
    private final BufferedImage heartImage; // processed image 
    private final int heartWidth = 12;
    private final int heartHeight = 12;
    private final int spacing = 18; // pixel step between hearts

    public HeartsHUD(Anchor anchor, int marginX, int posY) {
        this.anchor = anchor;
        this.marginX = marginX;
        this.posY = posY;

        BufferedImage img = null;
        try {
            BufferedImage loaded = ImageLoader.load("HP.png");
            if (loaded != null) {
                // fully transparent
                BufferedImage argb = new BufferedImage(loaded.getWidth(), loaded.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < loaded.getHeight(); y++) {
                    for (int x = 0; x < loaded.getWidth(); x++) {
                        int rgb = loaded.getRGB(x, y);
                        int a = (rgb >> 24) & 0xFF;
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;
                        if (r == 0 && g == 0 && b == 0) {
                            // pure black -> transparent
                            argb.setRGB(x, y, 0);
                        } else {
                            int outA = a == 0 ? 255 : a;
                            int out = (outA << 24) | (r << 16) | (g << 8) | b;
                            argb.setRGB(x, y, out);
                        }
                    }
                }
                img = argb;
            }
        } catch (RuntimeException e) {
            img = null;
        }
        this.heartImage = img;
    }

    /**
     * Draw hearts on screen.
     *
     * @param g          GraphicsHandler to draw with
     * @param screenWidth current screen width (used for RIGHT anchor alignment)
     * @param hearts     number of filled hearts to draw
     * @param maxHearts  number of heart slots to draw
     */
    public void draw(GraphicsHandler g, int screenWidth, int hearts, int maxHearts) {
        // default: treat all partial hearts as full (backwards-compatible)
        draw(g, screenWidth, hearts, maxHearts, -1, -1);
    }

    /**
     * Draw hearts with knowledge of the current partial heart HP.
     * @param g graphics handler
     * @param screenWidth screen width for right anchor
     * @param hearts number of full hearts
     * @param maxHearts total heart slots
     * @param currentHeartHP current HP within the active (last) heart, or -1 if unknown
     * @param heartHPMax maximum HP per heart, or -1 if unknown
     */
    public void draw(GraphicsHandler g, int screenWidth, int hearts, int maxHearts, int currentHeartHP, int heartHPMax) {
        if (maxHearts <= 0) return;

        int slotStride = spacing; // pixel step between heart origins

        int startX;
        if (anchor == Anchor.LEFT) {
            startX = marginX;
        } else {
            startX = screenWidth - marginX - heartWidth - (maxHearts - 1) * slotStride;
        }

        for (int i = 0; i < maxHearts; i++) {
            int x = startX + i * slotStride;
            boolean fullyFilled = i < hearts; // full stocks
            boolean isCurrentPartial = (i == hearts) && (currentHeartHP >= 0 && heartHPMax > 0);

            if (heartImage != null) {
                if (fullyFilled) {
                    g.drawImage(heartImage, x, posY, heartWidth, heartHeight);
                } else if (isCurrentPartial) {
                    // draw heart image but darken if empty
                    if (currentHeartHP <= 0) {
                        // draw a black rectangle to indicate empty
                        g.drawFilledRectangle(x, posY, heartWidth, heartHeight, Color.BLACK);
                    } else {
                        g.drawImage(heartImage, x, posY, heartWidth, heartHeight);
                    }
                } else {
                    // empty slot
                    g.drawFilledRectangle(x, posY, heartWidth, heartHeight, Color.BLACK);
                }
            } else {
                // fallback: draw red for full, black for empty, gray for unknown partial
                Color fill;
                if (fullyFilled) fill = Color.RED;
                else if (isCurrentPartial) fill = currentHeartHP <= 0 ? Color.BLACK : Color.LIGHT_GRAY;
                else fill = Color.BLACK;
                g.drawFilledRectangle(x, posY, heartWidth, heartHeight, fill);
                g.drawRectangle(x, posY, heartWidth, heartHeight, Color.BLACK, 1);
            }
        }
    }
}
