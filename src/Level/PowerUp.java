package Level;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import Utils.ImageUtils;
import GameObject.ImageEffect;
import GameObject.Rectangle;
import Engine.GraphicsHandler;
import java.awt.image.BufferedImage;

// Simple power-up entity that sits on the map and can be collected by players
public class PowerUp extends MapEntity {

    private static BufferedImage img;
    private static BufferedImage fallback;

    public PowerUp(float x, float y) {
        super(x, y, buildFrame());
    }

    private static Frame buildFrame() {
        try {
            img = ImageLoader.load("powerupball.png");
            return new FrameBuilder(img)
                    .withScale(2)
                    .withBounds(0, 0, Math.max(8, img.getWidth() / 2), Math.max(8, img.getHeight() / 2))
                    .withImageEffect(ImageEffect.NONE)
                    .build();
        } catch (Exception e) {
            // fallback to a tiny transparent image so the AnimatedSprite machinery always has a non-null frame
            fallback = ImageUtils.createSolidImage(new java.awt.Color(255, 0, 255), 8, 8);
            return new FrameBuilder(fallback)
                    .withScale(1)
                    .withBounds(0, 0, 8, 8)
                    .withImageEffect(ImageEffect.NONE)
                    .build();
        }
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
    }

    public Rectangle getPickupBounds() {
        return getBounds();
    }
}
