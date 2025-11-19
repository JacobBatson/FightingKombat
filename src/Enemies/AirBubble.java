package Enemies;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.MapEntity;
import Level.MapEntityStatus;
import Level.Player;
import Utils.Direction;
import Utils.Point;

import java.util.HashMap;

// AirBubble: travels in a straight line, disappears on collision, uses air_bubble.png spritesheet
public class AirBubble extends Enemy {
    private float movementSpeed;
    private int existenceFrames;
    private int damage = 20;

    public AirBubble(Point location, float movementSpeed, int existenceFrames) {
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("air_bubble.png"), 7, 7), "DEFAULT");
        this.movementSpeed = movementSpeed;
        this.existenceFrames = existenceFrames;
        initialize();
    }

    public AirBubble(Point location, float movementSpeed, int existenceFrames, int damage) {
        this(location, movementSpeed, existenceFrames);
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (hasCollided) {
            this.mapEntityStatus = MapEntityStatus.REMOVED;
        }
    }

    @Override
    public void touchedPlayer(Player player) {
        super.touchedPlayer(player);
        this.mapEntityStatus = MapEntityStatus.REMOVED;
    }

    @Override
    public void update(Player player) {
        if (existenceFrames == 0) {
            this.mapEntityStatus = MapEntityStatus.REMOVED;
        } else {
            moveXHandleCollision(movementSpeed);
            super.update(player);
        }
        existenceFrames--;
    }

    public void handleMapEntityCollision(MapEntity mapEntity) {
        this.mapEntityStatus = MapEntityStatus.REMOVED;
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("DEFAULT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                        .withScale(3)
                        .withBounds(1, 1, 5, 5)
                        .build()
            });
        }};
    }
}
