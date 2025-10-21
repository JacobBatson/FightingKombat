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
import Enemies.WaterShot;
import Utils.Point;
import Level.PlayerState;
import Utils.AirGroundState;
import Utils.Direction;

import java.util.HashMap;
import GameObject.Rectangle;

public class Player2 extends MapEntity {
    protected Key FIREBALL_KEY = Key.ENTER;
    protected java.util.List<Fireball> fireballs = new java.util.ArrayList<>();
    protected Key PUNCH_KEY = Key.SHIFT; // keep it simple; broadly available

    private static final int HEART_HP = 100;
    private int maxHearts = 3;
    private int hearts = maxHearts;
    private int heartHP = HEART_HP;
    private int invulnFrames = 0;

    protected int punchDuration = 0;
    protected final int MAX_PUNCH_DURATION = 20;
    protected PlayerState previousNonPunchState = PlayerState.STANDING;

    protected float walkSpeed = 2.3f;
    protected float gravity = 0.5f;
    protected float jumpHeight = 14.5f;
    protected float jumpDegrade = 0.5f;
    protected float terminalVelocityY = 6f;
    protected float momentumYIncrease = 0.5f;

    protected float jumpForce = 0;
    protected float momentumY = 0;
    protected float moveAmountX, moveAmountY;
    protected float lastAmountMovedX, lastAmountMovedY;

    protected PlayerState playerState;
    protected PlayerState previousPlayerState;
    protected Direction facingDirection;
    protected AirGroundState airGroundState;
    protected AirGroundState previousAirGroundState;

    protected KeyLocker keyLocker = new KeyLocker();
    protected Key JUMP_KEY = Key.UP;
    protected Key MOVE_LEFT_KEY = Key.LEFT;
    protected Key MOVE_RIGHT_KEY = Key.RIGHT;

    private String characterSpritePathUsed;

    public Player2(float x, float y, String characterSpritePath, int spriteWidth, int spriteHeight) {
        super(x, y, new SpriteSheet(ImageLoader.load(characterSpritePath), spriteWidth, spriteHeight), "STAND_LEFT");
        facingDirection = Direction.LEFT;
        airGroundState = AirGroundState.AIR;
        previousAirGroundState = airGroundState;
        playerState = PlayerState.STANDING;
        previousPlayerState = playerState;
        this.characterSpritePathUsed = characterSpritePath;
    }

    // Keybind getters
    public Engine.Key getJumpKey() { return JUMP_KEY; }
    public Engine.Key getMoveLeftKey() { return MOVE_LEFT_KEY; }
    public Engine.Key getMoveRightKey() { return MOVE_RIGHT_KEY; }
    public Engine.Key getPunchKey() { return PUNCH_KEY; }
    public Engine.Key getFireballKey() { return FIREBALL_KEY; }

    public Player2(float x, float y) {
        this(x, y, "Fire_Sprite.png", 64, 64);
    }

    public void update() {
        moveAmountX = 0;
        moveAmountY = 0;
        applyGravity();

        do {
            previousPlayerState = playerState;
            handlePlayerState();
        } while (previousPlayerState != playerState);

        previousAirGroundState = airGroundState;

        lastAmountMovedX = super.moveXHandleCollision(moveAmountX);
        lastAmountMovedY = super.moveYHandleCollision(moveAmountY);

        handlePlayerAnimation();
        updateLockedKeys();

        // Shoot (Enter)
        if (Keyboard.isKeyDown(FIREBALL_KEY) && !keyLocker.isKeyLocked(FIREBALL_KEY)) {
            keyLocker.lockKey(FIREBALL_KEY);

            float fbSpeed = 4.0f;
            int fbFrames = 60;
            Utils.Point offset = getFireballSpawnOffset();
            float fbX = this.x + offset.x;
            float fbY = this.y + offset.y;
            float speed = (facingDirection == Direction.RIGHT) ? fbSpeed : -fbSpeed;

            Fireball shot = isWaterSkin()
                    ? new WaterShot(new Point(fbX, fbY), speed, fbFrames)
                    : new Fireball(new Point(fbX, fbY), speed, fbFrames);

            // critical: make projectile camera-aware
            shot.setMap(this.map);
            fireballs.add(shot);
        }
        if (Keyboard.isKeyUp(FIREBALL_KEY)) {
            keyLocker.unlockKey(FIREBALL_KEY);
        }

        // Update shots
        java.util.Iterator<Fireball> it = fireballs.iterator();
        while (it.hasNext()) {
            Fireball fb = it.next();
            fb.update(null);
            if (fb.getMapEntityStatus() == Level.MapEntityStatus.REMOVED) {
                it.remove();
            }
        }

        if (invulnFrames > 0) invulnFrames--;

        super.update();
    }

    private Utils.Point getFireballSpawnOffset() {
        float dx = (facingDirection == Direction.RIGHT) ? (this.getWidth() - 8f) : (-8f);
        float dy = (this.getHeight() * 0.40f);
        String anim = (this.currentAnimationName == null) ? "" : this.currentAnimationName;
        if (anim.contains("PUNCH")) dy -= 6f;
        if (anim.contains("JUMP") || anim.contains("FALL")) dy -= 10f;
        return new Utils.Point(Math.round(dx), Math.round(dy));
    }

    protected void applyGravity() { moveAmountY += gravity + momentumY; }

    protected void handlePlayerState() {
        switch (playerState) {
            case STANDING: playerStanding(); break;
            case WALKING:  playerWalking();  break;
            case JUMPING:  playerJumping();  break;
            case CROUCHING: break;
            case PUNCHING: playerPunching(); break;
        }
    }

    protected void playerStanding() {
        if (Keyboard.isKeyDown(PUNCH_KEY) && !keyLocker.isKeyLocked(PUNCH_KEY)) {
            keyLocker.lockKey(PUNCH_KEY);
            previousNonPunchState = PlayerState.STANDING;
            playerState = PlayerState.PUNCHING;
            punchDuration = 0;
        } else if (Keyboard.isKeyDown(MOVE_LEFT_KEY) || Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.WALKING;
        } else if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }
    }

    protected void playerWalking() {
        if (Keyboard.isKeyDown(PUNCH_KEY) && !keyLocker.isKeyLocked(PUNCH_KEY)) {
            keyLocker.lockKey(PUNCH_KEY);
            previousNonPunchState = PlayerState.WALKING;
            playerState = PlayerState.PUNCHING;
            punchDuration = 0;
        } else if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
            moveAmountX -= walkSpeed;
            facingDirection = Direction.LEFT;
        } else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            moveAmountX += walkSpeed;
            facingDirection = Direction.RIGHT;
        } else if (Keyboard.isKeyUp(MOVE_LEFT_KEY) && Keyboard.isKeyUp(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.STANDING;
        }

        if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }
    }

    protected void playerJumping() {
        if (Keyboard.isKeyDown(PUNCH_KEY) && !keyLocker.isKeyLocked(PUNCH_KEY)) {
            keyLocker.lockKey(PUNCH_KEY);
            previousNonPunchState = PlayerState.JUMPING;
            playerState = PlayerState.PUNCHING;
            punchDuration = 0;
        }

        if (previousAirGroundState == AirGroundState.GROUND && airGroundState == AirGroundState.GROUND) {
            airGroundState = AirGroundState.AIR;
            jumpForce = jumpHeight;
            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;
                if (jumpForce < 0) jumpForce = 0;
            }
        } else if (airGroundState == AirGroundState.AIR) {
            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;
                if (jumpForce < 0) jumpForce = 0;
            }
            if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) moveAmountX -= walkSpeed;
            else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) moveAmountX += walkSpeed;
            if (moveAmountY > 0) increaseMomentum();
        } else if (previousAirGroundState == AirGroundState.AIR && airGroundState == AirGroundState.GROUND) {
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
        if (momentumY > terminalVelocityY) momentumY = terminalVelocityY;
    }

    protected void updateLockedKeys() {
        if (Keyboard.isKeyUp(JUMP_KEY))  keyLocker.unlockKey(JUMP_KEY);
        if (Keyboard.isKeyUp(PUNCH_KEY)) keyLocker.unlockKey(PUNCH_KEY);
    }

    protected void handlePlayerAnimation() {
        if (playerState == PlayerState.STANDING)
            currentAnimationName = facingDirection == Direction.RIGHT ? "STAND_RIGHT" : "STAND_LEFT";
        else if (playerState == PlayerState.WALKING)
            currentAnimationName = facingDirection == Direction.RIGHT ? "WALK_RIGHT" : "WALK_LEFT";
        else if (playerState == PlayerState.JUMPING)
            currentAnimationName = (lastAmountMovedY <= 0)
                    ? (facingDirection == Direction.RIGHT ? "JUMP_RIGHT" : "JUMP_LEFT")
                    : (facingDirection == Direction.RIGHT ? "FALL_RIGHT" : "FALL_LEFT");
        else if (playerState == PlayerState.PUNCHING)
            currentAnimationName = facingDirection == Direction.RIGHT ? "PUNCH_RIGHT" : "PUNCH_LEFT";
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {}

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (direction == Direction.DOWN) {
            if (hasCollided) {
                momentumY = 0;
                airGroundState = AirGroundState.GROUND;
            } else {
                playerState = PlayerState.JUMPING;
                airGroundState = AirGroundState.AIR;
            }
        } else if (direction == Direction.UP && hasCollided) {
            jumpForce = 0;
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
        for (Fireball fb : fireballs) {
            fb.draw(graphicsHandler);
        }
    }

    public int getMaxHearts() { return maxHearts; }
    public int getHearts() { return hearts; }
    public int getHeartHP() { return heartHP; }
    public int getHeartHpMax() { return HEART_HP; }
    public boolean isKO() { return hearts <= 0 && heartHP <= 0; }

    public Rectangle getPunchHitbox() {
        if (playerState != PlayerState.PUNCHING) return null;
        Rectangle bounds = getBounds();
        float punchRange = 40f;
        float punchHeight = 30f;
        float punchX = (facingDirection == Direction.RIGHT)
                ? bounds.getX() + bounds.getWidth()
                : bounds.getX() - punchRange;
        float punchY = bounds.getY() + (bounds.getHeight() - punchHeight) / 2;
        return new Rectangle(punchX, punchY, (int) punchRange, (int) punchHeight);
    }

    public java.util.List<Fireball> getFireballs() { return this.fireballs; }
    public PlayerState getPlayerState() { return playerState; }
    public int getPunchDuration() { return punchDuration; }

    public void takeDamage(int amount) {
        if (amount <= 0 || invulnFrames > 0) return;

        int prevHearts = hearts;
        heartHP -= amount;
        if (heartHP <= 0 && hearts > 1) {
            hearts--; heartHP = HEART_HP;
        } else if (heartHP <= 0 && hearts == 1) {
            hearts = 0; heartHP = 0;
        }
        if (hearts < 0) hearts = 0;
        if (heartHP < 0) heartHP = 0;

        // If a full heart was lost, respawn at a random safe position on the map
        if (hearts < prevHearts && map != null) {
            int padding = Math.max(32, Math.round(Engine.ScreenManager.getScreenWidth() * 0.10f));
            Utils.Point tile = map.getRandomSafeSpawnTileInCamera(padding);
            if (tile.x >= 0) {
                Utils.Point pos = map.getPositionByTileIndex(Math.round(tile.x), Math.round(tile.y));
                this.setX(pos.x);
                this.setY(pos.y - this.getHeight());
            } else {
                Utils.Point spawn = map.getRandomSafeSpawnPositionInCamera();
                this.setX(spawn.x);
                this.setY(spawn.y);
            }
            // reset motion to avoid falling through tiles or carrying momentum
            this.momentumY = 0;
            this.jumpForce = 0;
            this.moveAmountX = 0;
            this.moveAmountY = 0;
            this.previousX = this.getX();
            this.previousY = this.getY();
            this.invulnFrames = 60;
        }

        invulnFrames = 3;
    }

    public Rectangle getCustomHitboxBounds() {
        Rectangle bounds = getBounds();
        int extra = 20;
        int hitboxHeight = bounds.getHeight() + extra;
        int hitboxY = Math.round(bounds.getY()) - (extra / 2);
        return new Rectangle(Math.round(bounds.getX()) + 10, hitboxY,
                bounds.getWidth(), hitboxHeight);
    }

    private boolean isWaterSkin() {
        if (characterSpritePathUsed == null) return false;
        String p = characterSpritePathUsed.toLowerCase();
        return p.contains("water");
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("STAND_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 0, 0, 3, 30, false));
            put("STAND_LEFT",  SpriteSheet.createSequentialFrames(spriteSheet, 0, 0, 3, 30, true));
            put("WALK_RIGHT",  SpriteSheet.createSequentialFrames(spriteSheet, 1, 0, 3, 30, false));
            put("WALK_LEFT",   SpriteSheet.createSequentialFrames(spriteSheet, 1, 0, 3, 30, true));
            put("JUMP_RIGHT",  SpriteSheet.createSequentialFrames(spriteSheet, 2, 0, 3, 20, false));
            put("JUMP_LEFT",   SpriteSheet.createSequentialFrames(spriteSheet, 2, 0, 3, 20, true));
            put("FALL_RIGHT",  SpriteSheet.createSequentialFrames(spriteSheet, 3, 0, 3, 20, false));
            put("FALL_LEFT",   SpriteSheet.createSequentialFrames(spriteSheet, 3, 0, 3, 20, true));
            put("PUNCH_RIGHT", SpriteSheet.createSequentialFrames(spriteSheet, 4, 0, 1, 15, false));
            put("PUNCH_LEFT",  SpriteSheet.createSequentialFrames(spriteSheet, 4, 0, 1, 15, true));
        }};
    }
}
