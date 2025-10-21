package Enemies;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.SpriteSheet;
import Level.MapEntityStatus;
import Level.Player;
import Utils.Direction;
import Utils.Point;
import GameObject.Frame;
import Level.MapEntity;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * RockShot: same behavior as Fireball, but renders using "Rock.png".
 * Auto-detects the image's width/height and treats it as a single-frame sheet.
 */
public class RockShot extends Fireball {

    // keep Fireball movement/lifespan behavior
    public RockShot(Point location, float movementSpeed, int existenceFrames) {
        super(location, movementSpeed, existenceFrames);
    }

    // Use the actual image size instead of hardcoding 7x7
    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet ignored) {
        // --- Load the PNG directly as ARGB (bypass ImageLoader so we keep transparency) ---
        java.awt.image.BufferedImage img;
        try {
            // Use your engine's resources path so it works the same everywhere
            img = javax.imageio.ImageIO.read(new java.io.File(Engine.Config.RESOURCES_PATH + "rock.png"));
        } catch (java.io.IOException e) {
            // Fallback to ImageLoader if direct read fails (may show black bg, but won't crash)
            img = Engine.ImageLoader.load("rock.png");
        }

        // Force ARGB type to preserve transparency
        if (img.getType() != java.awt.image.BufferedImage.TYPE_INT_ARGB) {
            java.awt.image.BufferedImage argb = new java.awt.image.BufferedImage(
                    img.getWidth(), img.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2 = argb.createGraphics();
            g2.setComposite(java.awt.AlphaComposite.SrcOver);
            g2.drawImage(img, 0, 0, null);
            g2.dispose();
            img = argb;
        }

        // Remove white/light background by making light pixels transparent
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int a = (rgb >> 24) & 0xFF;
                
                // More aggressive white/light removal - catch more shades
                if ((r > 200 && g > 200 && b > 200) || // Very light colors
                    (r > 220 && g > 220 && b > 220) || // Light colors
                    (r == 255 && g == 255 && b == 255)) { // Pure white
                    img.setRGB(x, y, 0x00000000); // Fully transparent
                }
            }
        }

        int w = img.getWidth();
        int h = img.getHeight();
        GameObject.SpriteSheet rockSheet = new GameObject.SpriteSheet(img, w, h);

        return new java.util.HashMap<String, GameObject.Frame[]>() {{
            put("DEFAULT", new GameObject.Frame[] {
                new Builders.FrameBuilder(rockSheet.getSprite(0, 0))
                    .withScale(0.045f)          // your chosen size
                    .withBounds(0, 0, w, h)    // hitbox = full sprite
                    .build()
            });
        }};
    }
}