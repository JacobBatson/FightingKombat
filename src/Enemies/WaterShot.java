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
 * WaterShot: same behavior as Fireball, but renders using "Water droplet.png".
 * Auto-detects the image's width/height and treats it as a single-frame sheet.
 */
public class WaterShot extends Fireball {

    // keep Fireball movement/lifespan behavior
    public WaterShot(Point location, float movementSpeed, int existenceFrames) {
        super(location, movementSpeed, existenceFrames);
    }

    // Use the actual image size instead of hardcoding 7x7
    @Override
public HashMap<String, Frame[]> loadAnimations(SpriteSheet ignored) {
    // --- Load the PNG directly as ARGB (bypass ImageLoader so we keep transparency) ---
    java.awt.image.BufferedImage img;
    try {
        // Use your engine's resources path so it works the same everywhere
        img = javax.imageio.ImageIO.read(new java.io.File(Engine.Config.RESOURCES_PATH + "Water Ball.png"));
    } catch (java.io.IOException e) {
        // Fallback to ImageLoader if direct read fails (may show black bg, but won't crash)
        img = Engine.ImageLoader.load("Water Ball.png");
    }

    if (img.getType() != java.awt.image.BufferedImage.TYPE_INT_ARGB) {
        java.awt.image.BufferedImage argb = new java.awt.image.BufferedImage(
                img.getWidth(), img.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = argb.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        img = argb;
    }

    int w = img.getWidth();
    int h = img.getHeight();
    GameObject.SpriteSheet waterSheet = new GameObject.SpriteSheet(img, w, h);

    return new java.util.HashMap<String, GameObject.Frame[]>() {{
        put("DEFAULT", new GameObject.Frame[] {
            new Builders.FrameBuilder(waterSheet.getSprite(0, 0))
                .withScale(0.02f)          // your chosen size
                .withBounds(0, 0, w, h)    // hitbox = full sprite
                .build()
        });
    }};
}
}