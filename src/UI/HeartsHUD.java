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
     */
    public void draw(GraphicsHandler g, int screenWidth, int hearts, int maxHearts) {
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
            boolean filled = i < hearts;

            if (heartImage != null) {
                // draw scaled to 12x12
                g.drawImage(heartImage, x, posY, heartWidth, heartHeight);
                if (!filled) {
                    // subtle outline for empty slots
                    g.drawRectangle(x, posY, heartWidth, heartHeight, Color.GRAY);
                }
            } else {
                // fallback: draw filled red for filled slots, gray for empty
                Color fill = filled ? Color.RED : Color.LIGHT_GRAY;
                g.drawFilledRectangle(x, posY, heartWidth, heartHeight, fill);
                g.drawRectangle(x, posY, heartWidth, heartHeight, Color.BLACK, 1);
            }
        }
    }
}
