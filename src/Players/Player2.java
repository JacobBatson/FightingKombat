package Players;

import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import GameObject.Frame;
import GameObject.SpriteSheet;
import Level.MapEntity;
import Enemies.Fireball;
import Utils.Point;
import Level.PlayerState;
import Utils.AirGroundState;
import Utils.Direction;

import java.awt.Color;

import java.util.Collections;
import java.util.HashMap;
import GameObject.Rectangle;

// Player2 - Uses arrow key controls for movement
public class Player2 extends MapEntity {
    // Fireball support
    protected Key FIREBALL_KEY = Key.ENTER;
    protected java.util.List<Fireball> fireballs = new java.util.ArrayList<>();
    // Movement values
    protected float walkSpeed = 2.3f;
    protected float gravity = 0.5f;
    protected float jumpHeight = 14.5f;
    protected float jumpDegrade = 0.5f;
    protected float terminalVelocityY = 6f;
    protected float momentumYIncrease = 0.5f;

    // Movement tracking
    protected float jumpForce = 0;
    protected float momentumY = 0;
    protected float moveAmountX, moveAmountY;
    protected float lastAmountMovedX, lastAmountMovedY;

    // State tracking
    protected PlayerState playerState;
    protected PlayerState previousPlayerState;
    protected Direction facingDirection;
    protected AirGroundState airGroundState;
    protected AirGroundState previousAirGroundState;

    // Health handling
    protected int maxHealth = 5;
    protected int currentHealth = maxHealth;
    protected int invincibilityFrames = 0;
    protected static final int MAX_INVINCIBILITY_FRAMES = 45;

    // Input handling
    protected KeyLocker keyLocker = new KeyLocker();
    protected Key JUMP_KEY = Key.UP;
    protected Key MOVE_LEFT_KEY = Key.LEFT;
    protected Key MOVE_RIGHT_KEY = Key.RIGHT;

    public Player2(float x, float y, String characterSpritePath, int spriteWidth, int spriteHeight) {
        super(x, y, new SpriteSheet(ImageLoader.load(characterSpritePath), spriteWidth, spriteHeight), "STAND_RIGHT");
        facingDirection = Direction.RIGHT;
        airGroundState = AirGroundState.AIR;
        previousAirGroundState = airGroundState;
        playerState = PlayerState.STANDING;
        previousPlayerState = playerState;
    }

    public Player2(float x, float y) {
        this(x, y, "Water_Sprite.png", 64, 64);
    }

    public void update() {
        moveAmountX = 0;
        moveAmountY = 0;

        applyGravity();

        if (invincibilityFrames > 0) {
            invincibilityFrames--;
        }

        // Update player state
        do {
            previousPlayerState = playerState;
            handlePlayerState();
        } while (previousPlayerState != playerState);

        previousAirGroundState = airGroundState;

        // Move player with collision detection
        lastAmountMovedX = super.moveXHandleCollision(moveAmountX);
        lastAmountMovedY = super.moveYHandleCollision(moveAmountY);

        handlePlayerAnimation();
        updateLockedKeys();

        // Fireball firing
        if (Keyboard.isKeyDown(FIREBALL_KEY) && !keyLocker.isKeyLocked(FIREBALL_KEY)) {
            keyLocker.lockKey(FIREBALL_KEY);
            float fbSpeed = 4.0f;
            int fbFrames = 60;
            float fbX = this.x + (facingDirection == Direction.RIGHT ? 24 : -7); // spawn at edge
            float fbY = this.y + -60; // roughly center vertically
            float speed = facingDirection == Direction.RIGHT ? fbSpeed : -fbSpeed;
            Fireball fireball = new Fireball(new Point(fbX, fbY), speed, fbFrames);
            fireball.setMap(map);
            fireballs.add(fireball);
        }
        if (Keyboard.isKeyUp(FIREBALL_KEY)) {
            keyLocker.unlockKey(FIREBALL_KEY);
        }

        // Update fireballs
        java.util.Iterator<Fireball> it = fireballs.iterator();
        while (it.hasNext()) {
            Fireball fb = it.next();
            fb.update(null); // no player needed for update
            if (fb.getMapEntityStatus() == Level.MapEntityStatus.REMOVED) {
                it.remove();
            }
        }

        // Update animation
        super.update();
    }

    public java.util.List<Fireball> getFireballs() {
        return Collections.unmodifiableList(fireballs);
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isInvincible() {
        return invincibilityFrames > 0;
    }

    public void takeDamage(int amount) {
        if (amount <= 0 || isInvincible()) {
            return;
        }

        currentHealth = Math.max(0, currentHealth - amount);
        invincibilityFrames = MAX_INVINCIBILITY_FRAMES;
    }

    public void heal(int amount) {
        if (amount <= 0) {
            return;
        }
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }

    protected void applyGravity() {
        moveAmountY += gravity + momentumY;
    }

    protected void handlePlayerState() {
        switch (playerState) {
            case STANDING:
                playerStanding();
                break;
            case WALKING:
                playerWalking();
                break;
            case JUMPING:
                playerJumping();
                break;
            case CROUCHING:
                // Crouching not implemented for simplified player
                break;
        }
    }

    protected void playerStanding() {
        // If walk left or walk right key is pressed, enter WALKING state
        if (Keyboard.isKeyDown(MOVE_LEFT_KEY) || Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.WALKING;
        }
        // If jump key is pressed, enter JUMPING state
        else if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }
    }

    protected void playerWalking() {
        // Move left
        if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
            moveAmountX -= walkSpeed;
            facingDirection = Direction.LEFT;
        }
        // Move right
        else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            moveAmountX += walkSpeed;
            facingDirection = Direction.RIGHT;
        } else if (Keyboard.isKeyUp(MOVE_LEFT_KEY) && Keyboard.isKeyUp(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.STANDING;
        }

        // Jump while walking
        if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }
    }

    protected void playerJumping() {
        // Setup jump if on ground
        if (previousAirGroundState == AirGroundState.GROUND && airGroundState == AirGroundState.GROUND) {
            currentAnimationName = facingDirection == Direction.RIGHT ? "JUMP_RIGHT" : "JUMP_LEFT";
            airGroundState = AirGroundState.AIR;
            jumpForce = jumpHeight;
            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;
                if (jumpForce < 0) {
                    jumpForce = 0;
                }
            }
        }
        // Continue jump in air
        else if (airGroundState == AirGroundState.AIR) {
            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;
                if (jumpForce < 0) {
                    jumpForce = 0;
                }
            }

            // Allow movement while in air
            if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
                moveAmountX -= walkSpeed;
            } else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
                moveAmountX += walkSpeed;
            }

            // Increase falling momentum
            if (moveAmountY > 0) {
                increaseMomentum();
            }
        }
        // Land on ground
        else if (previousAirGroundState == AirGroundState.AIR && airGroundState == AirGroundState.GROUND) {
            playerState = PlayerState.STANDING;
        }
    }

    protected void increaseMomentum() {
        momentumY += momentumYIncrease;
        if (momentumY > terminalVelocityY) {
            momentumY = terminalVelocityY;
        }
    }

    protected void updateLockedKeys() {
        if (Keyboard.isKeyUp(JUMP_KEY)) {
            keyLocker.unlockKey(JUMP_KEY);
        }
    }

    protected void handlePlayerAnimation() {
        if (playerState == PlayerState.STANDING) {
            this.currentAnimationName = facingDirection == Direction.RIGHT ? "STAND_RIGHT" : "STAND_LEFT";
        } else if (playerState == PlayerState.WALKING) {
            this.currentAnimationName = facingDirection == Direction.RIGHT ? "WALK_RIGHT" : "WALK_LEFT";
        } else if (playerState == PlayerState.JUMPING) {
            if (lastAmountMovedY <= 0) {
                this.currentAnimationName = facingDirection == Direction.RIGHT ? "JUMP_RIGHT" : "JUMP_LEFT";
            } else {
                this.currentAnimationName = facingDirection == Direction.RIGHT ? "FALL_RIGHT" : "FALL_LEFT";
            }
        }
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
    }

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        // Handle ground collision
        if (direction == Direction.DOWN) {
            if (hasCollided) {
                momentumY = 0;
                airGroundState = AirGroundState.GROUND;
            } else {
                playerState = PlayerState.JUMPING;
                airGroundState = AirGroundState.AIR;
            }
        }
        // Handle ceiling collision
        else if (direction == Direction.UP) {
            if (hasCollided) {
                jumpForce = 0;
            }
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);

        drawCustomHitbox(graphicsHandler, new Color(0, 0, 255, 100));

        for (Fireball fb : fireballs) {
            fb.draw(graphicsHandler);
        }
    }

    private void drawCustomHitbox(GraphicsHandler graphicsHandler, Color color) {
        Rectangle bounds = getBounds();
        int hitboxHeight = bounds.getHeight() + 40;
        int hitboxY = Math.round(bounds.getY()) - 125;

        graphicsHandler.drawFilledRectangle(
                Math.round(bounds.getX()) + 20,
                hitboxY,
                bounds.getWidth(),
                hitboxHeight,
                color);
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {
            {
                put("STAND_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 0, 0, 4, 30, false));

                put("STAND_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, 0, 0, 4, 30, true));

                put("WALK_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 2, 0, 4, 30, false));

                put("WALK_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, 2, 0, 4, 30, true));

                put("JUMP_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 3, 0, 4, 20, false));

                put("JUMP_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, 3, 0, 4, 20, true));

                put("FALL_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 4, 0, 4, 20, false));

                put("FALL_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, 4, 0, 4, 20, true));
            }
        };
    }
}
