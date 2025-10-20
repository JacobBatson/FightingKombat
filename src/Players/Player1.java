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

import java.util.HashMap;
import GameObject.Rectangle;

// NEW
import Enemies.WaterShot; // NEW: projectile that draws with "Water droplet.png"

// Player1 - Uses WASD controls for movement
public class Player1 extends MapEntity {
    // Fireball support
    protected Key FIREBALL_KEY = Key.E;
    protected java.util.List<Fireball> fireballs = new java.util.ArrayList<>();
    protected Key PUNCH_KEY = Key.F;
    // Health =
    private static final int HEART_HP = 100;
    private int maxHearts = 3;
    private int hearts = maxHearts;
    private int heartHP = HEART_HP;
    private int invulnFrames = 0;

    // Punch support
    protected int punchDuration = 0;
    protected final int MAX_PUNCH_DURATION = 20; // frames
    protected PlayerState previousNonPunchState = PlayerState.STANDING;
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

    // Input handling
    protected KeyLocker keyLocker = new KeyLocker();
    protected Key JUMP_KEY = Key.W;
    protected Key MOVE_LEFT_KEY = Key.A;
    protected Key MOVE_RIGHT_KEY = Key.D;

    // NEW: remember which sprite path this player was created with (to detect "water" skin)
    private String characterSpritePathUsed; // NEW

    public Player1(float x, float y, String characterSpritePath, int spriteWidth, int spriteHeight) {
        super(x, y, new SpriteSheet(ImageLoader.load(characterSpritePath), spriteWidth, spriteHeight), "STAND_RIGHT");
        facingDirection = Direction.RIGHT;
        airGroundState = AirGroundState.AIR;
        previousAirGroundState = airGroundState;
        playerState = PlayerState.STANDING;
        previousPlayerState = playerState;

        this.characterSpritePathUsed = characterSpritePath; // NEW
    }

    public Player1(float x, float y) {
        this(x, y, "Fire_Sprite.png", 64, 64);
    }

    public void update() {
        moveAmountX = 0;
        moveAmountY = 0;

        applyGravity();

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

        // Fireball firing (spawn relative to player bounds so it aligns across maps)
        if (Keyboard.isKeyDown(FIREBALL_KEY) && !keyLocker.isKeyLocked(FIREBALL_KEY)) {
            keyLocker.lockKey(FIREBALL_KEY);
            float fbSpeed = 4.0f;
            int fbFrames = 60;
            // compute spawn offset from current animation / facing so splash aligns with
            // hand
            Utils.Point offset = getFireballSpawnOffset();
            float fbX = this.x + (facingDirection == Direction.RIGHT ? 50 : 50);
            float fbY = this.y + offset.y;
            float speed = facingDirection == Direction.RIGHT ? fbSpeed : -fbSpeed;
            fireballs.add(new Fireball(new Point(fbX, fbY), speed, fbFrames));

            // NEW: if this player's sprite path indicates a water skin, swap to WaterShot
            if (isWaterSkin()) { // NEW
                fireballs.remove(fireballs.size() - 1); // NEW: remove the Fireball we just added
                fireballs.add(new WaterShot(new Point(fbX, fbY), speed, fbFrames)); 
            } // NEW
        }
        if (Keyboard.isKeyUp(FIREBALL_KEY)) {
            keyLocker.unlockKey(FIREBALL_KEY);
        }

        // Update fireballs and check for collision with this player
        java.util.Iterator<Fireball> it = fireballs.iterator();
        while (it.hasNext()) {
            Fireball fb = it.next();
            fb.update(null); // no player needed for update
            if (fb.getMapEntityStatus() == Level.MapEntityStatus.REMOVED) {
                it.remove();
            }
        }

        // invulnerability countdown
        if (invulnFrames > 0) {
            invulnFrames--;
        }

        // Update animation
        super.update();
    }

    // Return a spawn offset (relative to this.x,this.y) for where a fireball should
    // originate.
    private Utils.Point getFireballSpawnOffset() {
        float dx = facingDirection == Direction.RIGHT ? this.getWidth() - 8f : -8f;
        float dy = (this.getHeight() / 2f) - 8f;
        String anim = this.currentAnimationName == null ? "" : this.currentAnimationName;
        // if punching, spawn slightly higher (near fist)
        if (anim.contains("PUNCH")) {
            dy -= 6f;
        }
        // if jumping, raise spawn point more
        if (anim.contains("JUMP") || anim.contains("FALL")) {
            dy -= 10f;
        }
        return new Utils.Point(Math.round(dx), Math.round(dy));
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
            case PUNCHING:
                playerPunching();
                break;
        }
    }

    protected void playerStanding() {
        // If punch key is pressed, enter PUNCHING state
        if (Keyboard.isKeyDown(PUNCH_KEY) && !keyLocker.isKeyLocked(PUNCH_KEY)) {
            keyLocker.lockKey(PUNCH_KEY);
            previousNonPunchState = PlayerState.STANDING;
            playerState = PlayerState.PUNCHING;
            punchDuration = 0;
        }
        // If walk left or walk right key is pressed, enter WALKING state
        else if (Keyboard.isKeyDown(MOVE_LEFT_KEY) || Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.WALKING;
        }
        // If jump key is pressed, enter JUMPING state
        else if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }
    }

    protected void playerWalking() {
        // If punch key is pressed, enter PUNCHING state
        if (Keyboard.isKeyDown(PUNCH_KEY) && !keyLocker.isKeyLocked(PUNCH_KEY)) {
            keyLocker.lockKey(PUNCH_KEY);
            previousNonPunchState = PlayerState.WALKING;
            playerState = PlayerState.PUNCHING;
            punchDuration = 0;
        }
        // Move left
        else if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
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
        // If punch key is pressed while jumping, enter PUNCHING state
        if (Keyboard.isKeyDown(PUNCH_KEY) && !keyLocker.isKeyLocked(PUNCH_KEY)) {
            keyLocker.lockKey(PUNCH_KEY);
            previousNonPunchState = PlayerState.JUMPING;
            playerState = PlayerState.PUNCHING;
            punchDuration = 0;
        }

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

    protected void playerPunching() {

        punchDuration++;

        if (punchDuration >= MAX_PUNCH_DURATION) {
            punchDuration = 0;
            playerState = previousNonPunchState;
        }

        if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
            moveAmountX -= walkSpeed * 0.5f;
            facingDirection = Direction.LEFT;
        } else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            moveAmountX += walkSpeed * 0.5f;
            facingDirection = Direction.RIGHT;
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
        if (Keyboard.isKeyUp(PUNCH_KEY)) {
            keyLocker.unlockKey(PUNCH_KEY);
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
        } else if (playerState == PlayerState.PUNCHING) {
            this.currentAnimationName = facingDirection == Direction.RIGHT ? "PUNCH_RIGHT" : "PUNCH_LEFT";
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

        // Draw custom taller hitbox around the player
        // drawCustomHitbox(graphicsHandler, new Color(255, 0, 0, 100)); // Red
        // semi-transparent hitbox

        for (Fireball fb : fireballs) {
            fb.draw(graphicsHandler);
        }
    }

    // Health API
    public int getMaxHearts() {
        return maxHearts;
    }

    public int getHearts() {
        return hearts;
    }

    public int getHeartHP() {
        return heartHP;
    }

    public int getHeartHpMax() {
        return HEART_HP;
    }

    public boolean isKO() {
        return hearts <= 0 && heartHP <= 0;
    }

    // Method to get punch hitbox bounds for collision detection
    public Rectangle getPunchHitbox() {
        if (playerState != PlayerState.PUNCHING) {
            return null;
        }

        Rectangle bounds = getBounds();
        float punchRange = 40f;
        float punchHeight = 30f;

        float punchX, punchY;

        if (facingDirection == Direction.RIGHT) {
            punchX = bounds.getX() + bounds.getWidth();
        } else {
            punchX = bounds.getX() - punchRange;
        }

        punchY = bounds.getY() + (bounds.getHeight() - punchHeight) / 2;

        return new Rectangle(punchX, punchY, (int) punchRange, (int) punchHeight);
    }

    public java.util.List<Fireball> getFireballs() {
        return this.fireballs;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public int getPunchDuration() {
        return punchDuration;
    }

    public void takeDamage(int amount) {
        if (amount <= 0)
            return;
        if (invulnFrames > 0)
            return; // ignore while invulnerable

        // Health damage does not carry over between hearts
        heartHP -= amount;

        // If current heart is depleted, consume it and reset to full HP
        if (heartHP <= 0 && hearts > 1) {
            hearts--;
            heartHP = HEART_HP; // Reset to full HP for new heart
        } else if (heartHP <= 0 && hearts == 1) {
            // Last heart consumed
            hearts = 0;
            heartHP = 0;
        }

        // Ensure values don't go negative
        if (hearts < 0)
            hearts = 0;
        if (heartHP < 0)
            heartHP = 0;

        invulnFrames = 3;
    }

    // Custom method to draw a taller hitbox
    // private void drawCustomHitbox(GraphicsHandler graphicsHandler, Color color) {
    // Rectangle bounds = getBounds();
    // int hitboxHeight = bounds.getHeight() + 40; // Make hitbox 20 pixels taller
    // int hitboxY = Math.round(bounds.getY()) - 125; // Center the extra height
    // above the player

    // // Draw the taller hitbox
    // graphicsHandler.drawFilledRectangle(
    // Math.round(bounds.getX()) + 20,
    // hitboxY,
    // bounds.getWidth(),
    // hitboxHeight,
    // color);
    // }

    // Method to get custom hitbox bounds for collision detection
    public Rectangle getCustomHitboxBounds() {
        Rectangle bounds = getBounds();
        // Make hitbox slightly taller and centered on the player; avoid large negative
        // offsets
        int extra = 20;
        int hitboxHeight = bounds.getHeight() + extra;
        int hitboxY = Math.round(bounds.getY()) - (extra / 2);

        return new Rectangle(
                Math.round(bounds.getX()) + 10,
                hitboxY,
                bounds.getWidth(),
                hitboxHeight);
    }

    // NEW: detect if this player's skin is the water one (filename contains "water")
    private boolean isWaterSkin() { // NEW
        if (characterSpritePathUsed == null) return false; // NEW
        String p = characterSpritePathUsed.toLowerCase();  // NEW
        return p.contains("water");                        // NEW (covers "Water droplet.png")
    } // NEW

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {
            {
                put("STAND_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 0, 0, 3, 30, false));

                put("STAND_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, 0, 0, 3, 30, true));

                put("WALK_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 1, 0, 3, 30, false));

                put("WALK_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, 1, 0, 3, 30, true));

                put("JUMP_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 2, 0, 3, 20, false));

                put("JUMP_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, 2, 0, 3, 20, true));

                put("FALL_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 3, 0, 3, 20, false));

                put("FALL_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, 3, 0, 3, 20, true));

                put("PUNCH_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 4, 0, 1, 15, false));

                put("PUNCH_LEFT", SpriteSheet.createSequentialFrames(spriteSheet, 4, 0, 1, 15, true));
            }
        };
    }

    // Keybind getters
    public Engine.Key getJumpKey() { return JUMP_KEY; }
    public Engine.Key getMoveLeftKey() { return MOVE_LEFT_KEY; }
    public Engine.Key getMoveRightKey() { return MOVE_RIGHT_KEY; }
    public Engine.Key getCrouchKey() { return null; }
    public Engine.Key getPunchKey() { return PUNCH_KEY; }
    public Engine.Key getFireballKey() { return FIREBALL_KEY; }
}
