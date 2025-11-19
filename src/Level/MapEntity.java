package Level;

import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;

import java.util.HashMap;

// This class represents a map entity, which is any "entity" on a map besides the player
// it is basically a game object with a few extra features for handling things like respawning
public class MapEntity extends GameObject {
    protected MapEntityStatus mapEntityStatus = MapEntityStatus.ACTIVE;

    // if true, entity will continue to be updated even if off camera
    protected boolean isUpdateOffScreen = false;

    public MapEntity(float x, float y, SpriteSheet spriteSheet, String startingAnimation) {
        super(spriteSheet, x, y, startingAnimation);
    }

    public MapEntity(float x, float y, HashMap<String, Frame[]> animations, String startingAnimation) {
        super(x, y, animations, startingAnimation);
    }

    public MapEntity(float x, float y, Frame[] frames) {
        super(x, y, frames);
    }

    public MapEntity(float x, float y, Frame frame) {
        super(x, y, frame);
    }

    public MapEntity(float x, float y) {
        super(x, y);
    }

    public void initialize() {
        this.x = startPositionX;
        this.y = startPositionY;
        this.amountMovedX = 0;
        this.amountMovedY = 0;
        this.previousX = startPositionX;
        this.previousY = startPositionY;
        updateCurrentFrame();
    }

    public MapEntityStatus getMapEntityStatus() {
        return mapEntityStatus;
    }

    public void setMapEntityStatus(MapEntityStatus mapEntityStatus) {
        this.mapEntityStatus = mapEntityStatus;
    }

    public boolean isUpdateOffScreen() {
        return isUpdateOffScreen;
    }

    public void setIsUpdateOffScreen(boolean isUpdateOffScreen) {
        this.isUpdateOffScreen = isUpdateOffScreen;
    }

    // Apply knockback away from attackerX. multiplier scales horizontal and vertical impulse.
    public void applyKnockback(float attackerX, float multiplier) {
        if (multiplier <= 0)
            multiplier = 1.0f;
        float kbPixels = 20f * multiplier;
        if (attackerX < this.getX()) {
            this.setX(this.getX() + kbPixels);
        } else {
            this.setX(this.getX() - kbPixels);
        }
        // single-frame upward impulse scaled by multiplier
        // MapEntity doesn't define jumpForce/moveAmountY directly, but GameObject has fields used by Player subclasses.
        // If this concrete class (or its superclasses) define jumpForce/moveAmountY, update them via reflection
        try {
            Class<?> cur = this.getClass();
            while (cur != null) {
                try {
                    java.lang.reflect.Field jf = cur.getDeclaredField("jumpForce");
                    jf.setAccessible(true);
                    jf.setFloat(this, 0f);
                    break;
                } catch (NoSuchFieldException nsf) {
                    cur = cur.getSuperclass();
                }
            }
        } catch (Exception ex) {
            // ignore
        }
        try {
            Class<?> cur = this.getClass();
            while (cur != null) {
                try {
                    java.lang.reflect.Field maY = cur.getDeclaredField("moveAmountY");
                    maY.setAccessible(true);
                    float curVal = maY.getFloat(this);
                    maY.setFloat(this, curVal - 8f * multiplier);
                    break;
                } catch (NoSuchFieldException nsf) {
                    cur = cur.getSuperclass();
                }
            }
        } catch (Exception ex) {
            // ignore
        }
        this.previousX = this.getX();
        this.previousY = this.getY();
    }
}
